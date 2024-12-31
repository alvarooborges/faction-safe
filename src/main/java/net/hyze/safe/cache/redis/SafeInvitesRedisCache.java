package net.hyze.safe.cache.redis;

import com.google.common.collect.Maps;
import net.hyze.core.shared.cache.redis.RedisCache;
import net.hyze.safe.SafeProvider;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class SafeInvitesRedisCache implements RedisCache {

    private static final String KEY_PREFIX = "safes_invitations";

    // prefix:safe_id:user_id
    private static final BiFunction<Object, Object, String> NOMINATOR = (safeId, userId) -> {
        return String.format("%s:%s:%s", KEY_PREFIX, safeId, userId);
    };

    /**
     * Busca a lista de todos os convites enviados de um array de ids de cofres passados.
     *
     * @param safesIds - Array de ids de cofres
     * @return - Map contendo key como id do cofre e o valor com a lista de jogadores convidados.
     */
    public Map<Integer, Set<Integer>> fetchInvitesBySafes(int... safesIds) {
        try (Jedis jedis = SafeProvider.Redis.REDIS.provide().getResource()) {

            Pipeline pipeline = jedis.pipelined();

            Map<Integer, Response<Set<String>>> responses = Maps.newHashMap();

            for (int safeId : safesIds) {
                responses.put(safeId, pipeline.keys(NOMINATOR.apply(safeId, "*")));
            }

            pipeline.sync();

            return responses.entrySet().stream()
                    .map(entry -> Maps.immutableEntry(entry.getKey(), entry.getValue().get()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> {
                                return entry.getValue().stream()
                                        .map(key -> {
                                            String[] parts = key.split(":");
                                            return Integer.valueOf(parts[parts.length - 1]);
                                        })
                                        .collect(Collectors.toSet());
                            }
                    ));
        }
    }

    /**
     * Busca todos os convites recebidos armazenados no redis do usuário em passado.
     *
     * @param userId - ID do usuário que recebeu o convite
     * @return - Retonar um mapa com key sendo o safeId e o value sendo o senderId
     */
    public Map<Integer, Integer> fetchInvitesByTargetUser(int userId) {
        try (Jedis jedis = SafeProvider.Redis.REDIS.provide().getResource()) {
            // prefix:*:user_id
            Set<String> keys = jedis.keys(NOMINATOR.apply("*", userId));

            Pipeline pipeline = jedis.pipelined();

            Map<Integer, Response<String>> responses = Maps.newHashMap();

            for (String key : keys) {
                String[] parts = key.split(":");
                int safeId = Integer.valueOf(parts[1]);

                responses.put(safeId, pipeline.get(key));
            }

            pipeline.sync();

            return responses.entrySet()
                    .stream()
                    .map(entry -> Maps.immutableEntry(entry.getKey(), Integer.valueOf(entry.getValue().get())))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                    ));
        }
    }

    /**
     * Cria um novo convite para o jogador fazer parte de um cofre.
     *
     * @param senderId - Quem enviou o convite.
     * @param targetId - Quem vai receber o convite.
     * @param safeId   - ID do cofre que será compartilhado.
     */
    public void createInvite(int senderId, int targetId, int safeId) {
        try (Jedis jedis = SafeProvider.Redis.REDIS.provide().getResource()) {
            jedis.set(NOMINATOR.apply(safeId, targetId), String.valueOf(senderId));
            jedis.expire(NOMINATOR.apply(safeId, targetId), 60 * 5);
        }
    }

    /**
     * Deleta um convite recebido especifico.
     *
     * @param targetId - Usuário que recebeu o convite.
     * @param safeId   - ID do cofre que foi compartilhado.
     */
    public void deleteInvite(int targetId, int safeId) {
        try (Jedis jedis = SafeProvider.Redis.REDIS.provide().getResource()) {
            jedis.del(NOMINATOR.apply(safeId, targetId));
        }
    }
}
