package net.hyze.safe;

import lombok.Getter;
import net.hyze.core.shared.providers.MysqlDatabaseProvider;
import net.hyze.core.shared.providers.MysqlRepositoryProvider;
import net.hyze.core.shared.providers.RedisCacheProvider;
import net.hyze.core.shared.providers.RedisProvider;
import net.hyze.safe.cache.redis.SafeInvitesRedisCache;
import net.hyze.safe.storage.SafeRepository;

public class SafeProvider {

    @Getter
    private static SafeSettings settings;

    public static void prepare(MysqlDatabaseProvider mysqlProvider, RedisProvider redisProvider) {
        prepare(mysqlProvider, redisProvider, SafeSettings.builder().build());
    }

    public static void prepare(MysqlDatabaseProvider mysqlProvider, RedisProvider redisProvider, SafeSettings settings) {
        Redis.REDIS = redisProvider;

        Repositories.SAFE = new MysqlRepositoryProvider(
                () -> mysqlProvider,
                SafeRepository.class
        );

        Repositories.SAFE.prepare();

        SafeProvider.settings = settings;
    }

    public static class Cache {
        public static class Redis {
            public static final RedisCacheProvider<SafeInvitesRedisCache> INVITES = new RedisCacheProvider<>(new SafeInvitesRedisCache());
        }
    }

    public static class Repositories {

        public static MysqlRepositoryProvider<SafeRepository> SAFE;

    }

    public static class Redis {

        public static RedisProvider REDIS;

    }
}
