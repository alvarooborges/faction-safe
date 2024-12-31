package net.hyze.safe.storage;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.hyze.core.shared.providers.MysqlDatabaseProvider;
import net.hyze.core.shared.storage.repositories.MysqlRepository;
import net.hyze.core.shared.user.User;
import net.hyze.safe.Safe;
import net.hyze.safe.storage.specs.*;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SafeRepository extends MysqlRepository {

    public SafeRepository(MysqlDatabaseProvider databaseProvider) {
        super(databaseProvider);
    }

    /**
     * Busca todos os cofres de um usuário.
     *
     * @param user - usuário.
     * @return - List de cofres do usuário.
     */
    public Set<Safe> fetchSafesIds(User user) {
        return query(new SelectSafesIdsByUserSpec(user));
    }

    /**
     * Cria um novo cofre e associa a um usuário.
     *
     * @param user - usuário que será associado ao novo cofre
     * @return - ID do cofre criado.
     */
    public Safe create(User user) {
        int safeId = query(new CreateSafeSpec());

        associateSafeWithUser(safeId, user.getId());

        return new Safe(safeId, Material.CHEST, 0);
    }

    /**
     * Atualiza as informações de um cofre.
     *
     * @param safe - Cofre que será atualizado
     * @return - true se sucesso.
     */
    public boolean update(Safe safe) {
        return query(new UpdateSafeSpec(safe));
    }

    /**
     * Deleta um cofre.
     *
     * @param safeId - ID do cofre.
     * @return - true se sucesso.
     */
    public boolean delete(int safeId) {
        return query(new DeleteSafeSpec(safeId));
    }

    /**
     * Busca todos os containers de um cofre.
     *
     * @param safeId - ID do cofre
     * @return - map como key sendo a posição do container e array de itens no valor
     */
    public Map<Integer, ItemStack[]> fetchSafeContainers(Integer safeId) {
        return query(new SelectSafeContainersBySafeIdSpec(safeId));
    }

    /**
     * Desassocia um cofre de um usuário.
     *
     * @param safeId - ID do cofre.
     * @param userId - ID do usuário
     * @return - true se sucesso na operação
     */
    public boolean dissociateSafeWithUser(int safeId, int userId) {
        return query(new DissociateSafeWithUserSpec(safeId, userId));
    }

    /**
     * Busca todos os usuário associados ao cofre.
     *
     * @param safeId - ID do cofre.
     * @return - Set de ids dos usuários associados.
     */
    public Set<Integer> fetchAssociatedUsersIds(int safeId) {
        return query(new SelectAssociatedUsersIdsBySafeId(safeId));
    }

    /**
     * Associa um cofre ao usuário.
     *
     * @param safeId - ID do cofre.
     * @param userId - ID do usuário
     * @return - true se sucesso na operação
     */
    public boolean associateSafeWithUser(int safeId, int userId) {
        return query(new AssociateSafeWithUserSpec(safeId, userId));
    }

    /**
     * Atualiza ou deleta o container.
     * Se o container estiver vazio, ele será deletado.
     *
     * @param safeId    - ID do cofre.
     * @param position  - Posição do container.
     * @param inventory - Inventário.
     * @return - true se sucesso.
     */
    public boolean updateSafeContainer(int safeId, int position, Inventory inventory) {
        boolean isEmpty = Arrays.asList(inventory.getContents()).stream()
                .allMatch(Objects::isNull);

        if (isEmpty) {
            return query(new DeleteSafeContainerSpec(safeId, position));
        }

        return query(new SaveSafeContainerSpec(safeId, position, inventory.getContents()));
    }
}
