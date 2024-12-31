package net.hyze.safe.inventories;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.inventory.ConfirmInventory;
import net.hyze.core.spigot.inventory.PaginateInventory;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.HeadTexture;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.hyze.safe.Safe;
import net.hyze.safe.SafeController;
import net.hyze.safe.SafeProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class SafeReceivedInvitationsInventory extends PaginateInventory {

    private final User user;
    private final Player player;

    public SafeReceivedInvitationsInventory(User user, Player player) {
        super("Convites recebidos");

        this.user = user;
        this.player = player;

        backItem(new MainSafeInventory(user, player));


        Map<Integer, Integer> invites = SafeProvider.Cache.Redis.INVITES.provide()
                .fetchInvitesByTargetUser(user.getId());

        invites.forEach((safeId, senderId) -> {
            User sender = CoreProvider.Cache.Local.USERS.provide().get(senderId);

            if (sender == null) {
                return;
            }

            ItemBuilder baseIcon = ItemBuilder.of(HeadTexture.getPlayerHead(sender.getNick()))
                    .name("&6Convite")
                    .lore("&7Enviado por &f" + sender.getNick());

            ItemStack icon = baseIcon.clone().lore("", "&eClique para aceitar o convite.").make();
            ItemStack confirmIcon = baseIcon.clone().make();

            addItem(icon, event -> {
                Consumer<InventoryClickEvent> onDeny = denyClickEvent -> {
                    SafeProvider.Cache.Redis.INVITES.provide().deleteInvite(user.getId(), safeId);
                    Message.INFO.send(player, String.format("Convite de %s negado.", sender.getNick()));
                    player.openInventory(new SafeReceivedInvitationsInventory(user, player));
                };

                ConfirmInvite confirmInvite = new ConfirmInvite(sender, safeId);

                ConfirmInventory confirmInventory = ConfirmInventory.of(
                        confirmInvite::handleAccept,
                        confirmInvite::handleDeny,
                        confirmIcon
                );

                player.openInventory(confirmInventory.make("Aceitar convite"));
            });
        });
    }

    @RequiredArgsConstructor
    private class ConfirmInvite {
        private final User sender;
        private final int safeId;

        private void handleAccept(InventoryClickEvent event) {

            Integer currentSafeId = SafeController.getCurrentSafeId(user);

            if (SafeProvider.getSettings().getCuboid().contains(player.getLocation(), true) && currentSafeId != null) {
                Message.ERROR.send(player, "Você não pode aceitar convites dentro de um cofre. Use /cofre sair.");
                return;
            }

            Set<Safe> safes = SafeProvider.Repositories.SAFE.provide().fetchSafesIds(user);

            Optional<Integer> selfEmptySafeId = safes.stream()
                    .map(Safe::getId)
                    .filter(selfSafeId -> {
                        return SafeProvider.Repositories.SAFE.provide().fetchSafeContainers(selfSafeId)
                                .isEmpty();
                    })
                    .filter(selfSafeId -> {
                        return SafeProvider.Repositories.SAFE.provide().fetchAssociatedUsersIds(selfSafeId)
                                .stream()
                                .allMatch(memberId -> memberId.equals(user.getId()));
                    })
                    .findFirst();

            if (!selfEmptySafeId.isPresent()) {
                player.closeInventory();
                Message.ERROR.send(player, "Você precisa ter um cofre totalmente vazio para fazer parte de outro cofre.");
                return;
            }

            // Desassociando cofre vazio
            SafeProvider.Repositories.SAFE.provide()
                    .dissociateSafeWithUser(selfEmptySafeId.get(), user.getId());

            // Deletando cofre vazio
            SafeProvider.Repositories.SAFE.provide()
                    .delete(selfEmptySafeId.get());

            // Deletando convite
            SafeProvider.Cache.Redis.INVITES.provide()
                    .deleteInvite(user.getId(), safeId);

            // Associando com o novo cofre
            SafeProvider.Repositories.SAFE.provide()
                    .associateSafeWithUser(safeId, user.getId());

            Message.SUCCESS.send(player, String.format("Convite de %s aceito.", sender.getNick()));
            player.openInventory(new SafeReceivedInvitationsInventory(user, player));
        }

        private void handleDeny(InventoryClickEvent event) {
            SafeProvider.Cache.Redis.INVITES.provide().deleteInvite(user.getId(), safeId);
            Message.INFO.send(player, String.format("Convite de %s negado.", sender.getNick()));
            player.openInventory(new SafeReceivedInvitationsInventory(user, player));
        }
    }


}
