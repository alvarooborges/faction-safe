package net.hyze.safe.inventories.management;

import com.google.common.collect.Lists;
import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.CoreSpigotConstants;
import net.hyze.core.spigot.CoreSpigotPlugin;
import net.hyze.core.spigot.inventory.PaginateInventory;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.ItemBuilder;
import net.hyze.core.spigot.misc.utils.ItemStackUtils;
import net.hyze.core.spigot.misc.utils.TranslateItem;
import net.hyze.safe.Safe;
import net.hyze.safe.SafeProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.material.MaterialData;

import java.util.List;

public class SelectIconInventory extends PaginateInventory {

    public static final List<MaterialData> MATERIAL_DATA_LIST = Lists.newArrayList();

    static {
        MATERIAL_DATA_LIST.add(new MaterialData(Material.DIAMOND_CHESTPLATE, (byte) 0));
        MATERIAL_DATA_LIST.add(new MaterialData(Material.DIAMOND_SWORD, (byte) 0));
        MATERIAL_DATA_LIST.add(new MaterialData(Material.DIAMOND_PICKAXE, (byte) 0));
        MATERIAL_DATA_LIST.add(new MaterialData(Material.POTION, (byte) 8193));
        MATERIAL_DATA_LIST.add(new MaterialData(Material.DIAMOND, (byte) 0));
        MATERIAL_DATA_LIST.add(new MaterialData(Material.BEDROCK, (byte) 0));
        MATERIAL_DATA_LIST.add(new MaterialData(Material.TNT, (byte) 0));
        MATERIAL_DATA_LIST.add(new MaterialData(Material.CHEST, (byte) 0));
    }

    public SelectIconInventory(User user, Player player, Safe safe, int safeIndex) {
        super("Escolha um ícone");

        backItem(new MainManagementInventory(user, player, safe, safeIndex));

        ItemBuilder currentIcon = ItemBuilder.of(safe.getIconMaterial(), 1, (short) safe.getMaterialData())
                .name("&aÍcone atual")
                .flags(ItemFlag.values());

        addMenu(48, currentIcon.make());

        MATERIAL_DATA_LIST.forEach(material -> {
            ItemBuilder icon = ItemBuilder.of(material.toItemStack(1))
                    .name("&b" + CoreSpigotConstants.TRANSLATE_ITEM.get(material.toItemStack(1)))
                    .flags(ItemFlag.values());

            if (icon.type() == currentIcon.type() && icon.durability() == currentIcon.durability()) {
                icon.lore("", "&aSelecionado.");
                addItem(icon.make());
                return;
            }

            icon.lore("", "&eClique para selecionar este ícone.");

            addItem(icon.make(), () -> {
                safe.setIconMaterial(material.getItemType());
                safe.setMaterialData(material.getData());

                SafeProvider.Repositories.SAFE.provide().update(safe);
                Message.SUCCESS.send(player, "Ícone atualizado.");
                player.openInventory(new SelectIconInventory(user, player, safe, safeIndex));
            });
        });
    }
}
