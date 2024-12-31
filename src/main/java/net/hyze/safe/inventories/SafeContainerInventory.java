package net.hyze.safe.inventories;

import net.hyze.core.spigot.inventory.CustomInventory;
import net.hyze.safe.SafeProvider;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public final class SafeContainerInventory extends CustomInventory {

    private final int safeId, position;

    public SafeContainerInventory(int safeId, int position, ItemStack[] contents) {
        super(54, "Cofre");
        this.safeId = safeId;
        this.position = position;

        for (int i = 0; i < contents.length && i < 54; i++) {
            setItem(i, contents[i]);
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {

    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        SafeProvider.Repositories.SAFE.provide().updateSafeContainer(safeId, position, this);

        EntityHuman human = ((CraftPlayer) event.getPlayer()).getHandle();
        MinecraftServer.getServer().getPlayerList().playerFileData.save(human);
    }
}
