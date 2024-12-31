package net.hyze.safe.inventories.management;

import java.util.Set;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.inventory.ConfirmInventory;
import net.hyze.core.spigot.inventory.CustomInventory;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.hyze.safe.Safe;
import net.hyze.safe.SafeController;
import net.hyze.safe.SafeProvider;
import net.hyze.safe.inventories.MainSafeInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;

public class MainManagementInventory extends CustomInventory {

    private final User user;
    private final Player player;
    private final Safe safe;
    private final int safeIndex;

    public MainManagementInventory(User user, Player player, Safe safe, int safeIndex) {
        super(4 * 9, "Gerenciamento Cofre #" + safeIndex);

        this.user = user;
        this.player = player;
        this.safe = safe;
        this.safeIndex = safeIndex;

        backItem(31, event -> {
            player.openInventory(new MainSafeInventory(user, player));
        });

        ItemBuilder editIcon = ItemBuilder.of(safe.getIconMaterial())
                .data(safe.getMaterialData())
                .name("&bÍcone")
                .lore("", "&eClique para alterar o ícone.")
                .flags(ItemFlag.values());

        setItem(12, editIcon.make(), event -> {
            player.openInventory(new SelectIconInventory(user, player, safe, safeIndex));
        });

        Set<Integer> userIds = SafeProvider.Repositories.SAFE.provide().fetchAssociatedUsersIds(safe.getId());

        if (userIds.size() > 1) {
            ItemBuilder abandonIcon = ItemBuilder.of(Material.DARK_OAK_DOOR_ITEM)
                    .name("&aAbandonar cofre")
                    .lore("Ao abandonar um cofre", "você perde o acesso", "aos itens guardados.")
                    .lore("", "&eClique para abandonar.")
                    .flags(ItemFlag.values());

            setItem(35, abandonIcon.make(), event -> {
                AbandonHandle abandonHandle = new AbandonHandle();

                player.openInventory(ConfirmInventory.of(
                        abandonHandle::handleAccept,
                        abandonHandle::handleDeny, abandonIcon.make()
                ).make("Abandonar Cofre #" + safeIndex));
            });
        }

        ItemBuilder teleportIcon = ItemBuilder.of(Material.ENDER_PEARL)
                .name("&bTeleporte")
                .lore("", "&eClique para ir até o cofre.")
                .flags(ItemFlag.values());

        setItem(14, teleportIcon.make(), event -> {
            player.closeInventory();
            SafeController.tryTeleportToSafe(user, player, safe.getId());
        });
    }

    private class AbandonHandle {

        private void handleDeny(InventoryClickEvent event) {
            player.openInventory(new MainManagementInventory(user, player, safe, safeIndex));
        }

        private void handleAccept(InventoryClickEvent event) {
            boolean allMatch = SafeProvider.Repositories.SAFE.provide().fetchAssociatedUsersIds(safe.getId())
                    .stream()
                    .allMatch(memberId -> memberId.equals(user.getId()));

            if (!allMatch) {
                // Desassociando com o cofre
                SafeProvider.Repositories.SAFE.provide()
                        .dissociateSafeWithUser(safe.getId(), user.getId());

                // Cria um novo cofre para o usuário para substituir o cofre abandonado
                SafeProvider.Repositories.SAFE.provide()
                        .create(user);

                player.closeInventory();

                if (SafeProvider.getSettings().getCuboid().contains(player.getLocation(), true)) {
                    player.performCommand("spawn");
                }

                Message.SUCCESS.send(player, "Você abandonou o cofre.");
                return;
            }

            boolean safeIsEmpty = SafeProvider.Repositories.SAFE.provide().fetchSafeContainers(safe.getId())
                    .isEmpty();

            if (!safeIsEmpty) {
                player.closeInventory();
                Message.ERROR.send(player, "Você precisa esvaziar os baús antes de abandona-lo.");
                return;
            }

            // Desassociando com o cofre
            SafeProvider.Repositories.SAFE.provide()
                    .dissociateSafeWithUser(safe.getId(), user.getId());

            // Deletando cofre vazio
            SafeProvider.Repositories.SAFE.provide()
                    .delete(safe.getId());

            // Cria um novo cofre para o usuário para substituir o cofre abandonado
            SafeProvider.Repositories.SAFE.provide()
                    .create(user);

            player.closeInventory();

            if (SafeProvider.getSettings().getCuboid().contains(player.getLocation(), true)) {
                player.performCommand("spawn");
            }

            Message.SUCCESS.send(player, "Você abandonou o cofre.");
        }
    }
}
