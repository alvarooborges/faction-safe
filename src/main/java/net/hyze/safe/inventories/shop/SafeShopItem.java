package net.hyze.safe.inventories.shop;

import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.shop.ShopItem;
import net.hyze.core.spigot.misc.shop.module.AbstractModule;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.hyze.safe.Safe;
import net.hyze.safe.SafeProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SafeShopItem extends ShopItem {

    public SafeShopItem(int index, AbstractModule... modules) {
        super("Cofre #" + index, modules);
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "&7Compre este cofre adicional!"
        };
    }

    @Override
    public ItemBuilder getIcon() {
        return ItemBuilder.of(Material.STAINED_GLASS_PANE)
                .durability(4);
    }

    @Override
    public void onClick(Player player, User user, AbstractModule.State state, InventoryClickEvent event) {
        if (state == AbstractModule.State.SUCCESS) {

            int previousBalance = user.getRealCash();

            Runnable callback = () -> {
                Safe defaultSafe = SafeProvider.Repositories.SAFE.provide().create(user);

                if (defaultSafe == null) {
                    Message.ERROR.send(player, "Algo de errado aconteceu ao tentar criar o seu cofre!");
                    return;
                } else {
                    player.closeInventory();
                    Message.SUCCESS.send(player, "Você comprou um cofre!");

                    /*
                    Log.Cash.add(
                            user,
                            CoreProvider.getApp().getServer().getId(),
                            "1xSAFE",
                            -SafeProvider.getSettings().getCashPrice(),
                            previousBalance,
                            user.getRealCash()
                    );
                     */

                }
            };

            boolean value = false;

            if (modules != null) {
                for (AbstractModule module : modules) {
                    value |= module.transaction(user, player, callback);
                }
            }

            if (!value) {
                callback.run();
            }
        }
    }
}
