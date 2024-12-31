package net.hyze.safe.inventories;

import com.google.common.collect.Lists;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.inventory.ConfirmInventory;
import net.hyze.core.spigot.inventory.PaginateInventory;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.HeadTexture;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.hyze.safe.Safe;
import net.hyze.safe.SafeProvider;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SafeSentInvitationsInventory extends PaginateInventory {
    protected SafeSentInvitationsInventory(User user, Player player) {
        super("Convites enviados");

        backItem(new MainSafeInventory(user, player));

        Set<Safe> safes = SafeProvider.Repositories.SAFE.provide().fetchSafesIds(user);

        Map<Integer, Set<Integer>> invites = SafeProvider.Cache.Redis.INVITES.provide()
                .fetchInvitesBySafes(ArrayUtils.toPrimitive(safes.stream().map(Safe::getId).toArray(Integer[]::new)));

        invites.entrySet().stream()
                .forEach(entry -> {

                    entry.getValue().forEach(targetId -> {
                        User target = CoreProvider.Cache.Local.USERS.provide().get(targetId);

                        if (target == null) {
                            return;
                        }

                        int safeId = entry.getKey();

                        List<Integer> safesIdsList = Lists.newArrayList(safes).stream()
                                .map(Safe::getId)
                                .collect(Collectors.toList());

                        int safeIndex = safesIdsList.indexOf(safeId);

                        if (safeIndex < 0) {
                            return;
                        }

                        ItemBuilder baseIcon = ItemBuilder.of(HeadTexture.getPlayerHead(target.getNick()))
                                .name("&6Cofre #" + (safeIndex + 1))
                                .lore("&7Enviado para &f" + target.getNick());

                        ItemStack icon = baseIcon.clone().lore("", "&eClique para excluir o convite.").make();
                        ItemStack confirmIcon = baseIcon.clone().make();

                        addItem(icon, event -> {
                            ConfirmInventory confirmInventory = ConfirmInventory.of(
                                    acceptEvent -> {
                                        SafeProvider.Cache.Redis.INVITES.provide().deleteInvite(targetId, safeId);
                                        Message.INFO.send(player, String.format("Convite para %s excluído.", target.getNick()));
                                        player.openInventory(new SafeSentInvitationsInventory(user, player));
                                    },
                                    cancelEvent -> {
                                        player.openInventory(new SafeSentInvitationsInventory(user, player));
                                    },
                                    confirmIcon
                            );

                            player.openInventory(confirmInventory.make("Excluir convite"));
                        });
                    });
                });
    }
}
