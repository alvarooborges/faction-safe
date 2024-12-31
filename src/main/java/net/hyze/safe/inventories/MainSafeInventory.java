package net.hyze.safe.inventories;

import com.google.common.collect.Lists;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.shop.ShopInventory;
import net.hyze.core.spigot.misc.shop.module.AbstractModule;
import net.hyze.core.spigot.misc.shop.module.GroupModule;
import net.hyze.core.spigot.misc.shop.module.currency.CurrencyModule;
import net.hyze.core.spigot.misc.shop.module.currency.prices.CashPrice;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.hyze.economy.shop.module.currency.prices.CoinPrice;
import net.hyze.safe.Safe;
import net.hyze.safe.SafeProvider;
import net.hyze.safe.inventories.management.MainManagementInventory;
import net.hyze.safe.inventories.shop.SafeShopItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.Set;

public final class MainSafeInventory extends ShopInventory {

    public MainSafeInventory(User user, Player player) {
        super(36, "Escolha um cofre", user);

        Set<Safe> safes = SafeProvider.Repositories.SAFE.provide().fetchSafesIds(user);

        if (safes.isEmpty()) {
            Safe defaultSafe = SafeProvider.Repositories.SAFE.provide().create(user);

            if (defaultSafe == null) {
                Message.ERROR.send(player, "Algo de errado aconteceu ao tentar criar o seu cofre!");
                return;
            }

            safes.add(defaultSafe);
        }

        int slot = 12;
        int index = 1;

        for (Safe safe : safes) {
            Set<Integer> userIds = SafeProvider.Repositories.SAFE.provide().fetchAssociatedUsersIds(safe.getId());

            int currentIndex = index++;

            ItemBuilder safeIcon = ItemBuilder.of(safe.getIconMaterial())
                    .data(safe.getMaterialData())
                    .name("&bCofre #" + currentIndex)
                    .flags(ItemFlag.values());

            if (SafeProvider.getSettings().isAllowFriend()) {
                safeIcon.lore("&7Membros:");

                for (int userId : userIds) {
                    User member = CoreProvider.Cache.Local.USERS.provide().get(userId);

                    if (member != null) {
                        safeIcon.lore(" &8\u25AA " + member.getNick());
                    }
                }
            }

            safeIcon.lore(
                    "",
                    "&7Use o comando &f/cofre " + currentIndex + " &7para ir até ele.",
                    "&eClique para gerenciar."
            );

            setItem(slot++, safeIcon.make(), (event) -> {
                player.openInventory(new MainManagementInventory(user, player, safe, currentIndex));
            });
        }

        int initialSlot = slot;
        for (int i = slot; i <= 14; i++) {
            if (i == initialSlot) {
                int currentIndex = index++;
                List<AbstractModule> modules = Lists.newArrayList();

                modules.add(new CurrencyModule(
                        new CashPrice(SafeProvider.getSettings().getCashPrice()),
                        new CoinPrice(SafeProvider.getSettings().getCoinsPrice())
                ));

                if (currentIndex >= 3) {
                    modules.add(new GroupModule(Group.DIVINE));
                }

                AbstractModule[] modulesArray = modules.toArray(new AbstractModule[0]);
                setItem(slot++, new SafeShopItem(currentIndex, modulesArray));
            } else {
                ItemBuilder icon = ItemBuilder.of(Material.BARRIER)
                        .name("&cCofre #" + index++)
                        .flags(ItemFlag.values());

                setItem(slot++, icon.make());
            }
        }

        if (SafeProvider.getSettings().isAllowFriend()) {
            {
                ItemBuilder icon = ItemBuilder.of(Material.PAPER)
                        .name("&6Convites enviados")
                        .lore(
                                "&7Use &f/cofre convidar [nick] &7para",
                                "&7convidar alguem para o seu cofre.",
                                "",
                                "&eClique para ver os convites enviados."
                        );

                setItem(30, icon.make(), event -> {
                    player.openInventory(new SafeSentInvitationsInventory(user, player));
                });
            }
        }

        {
            ItemBuilder icon = ItemBuilder.of(Material.BOOK)
                    .name("&eInformações")
                    .lore(
                            "&7Com os cofres você pode guardar",
                            "&7seus itens em segurança, sem ter",
                            "&7medo de ser invadido!",
                            "",
                            "&7Você pode comprar mais cofres",
                            "&7utilizando Cubos e Moedas. O máximo de",
                            "&7cofres que você pode ter são 3."
                    );

            setItem(31, icon.make());
        }

        if (SafeProvider.getSettings().isAllowFriend()) {
            {
                ItemBuilder icon = ItemBuilder.of(Material.MAP)
                        .name("&aConvites recebidos")
                        .lore(
                                "Aceite convites e faça parte de",
                                "outros cofres.",
                                "",
                                "&eClique para ver os convites recebidos."
                        );

                setItem(32, icon.make(), event -> {
                    player.openInventory(new SafeReceivedInvitationsInventory(user, player));
                });
            }
        }
    }
}
