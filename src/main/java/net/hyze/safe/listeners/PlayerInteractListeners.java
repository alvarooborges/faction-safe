package net.hyze.safe.listeners;

import net.hyze.core.shared.CoreProvider;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.LocationUtils;
import net.hyze.safe.SafeController;
import net.hyze.safe.SafeProvider;
import net.hyze.safe.SafeSettings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;
import java.util.Optional;

public class PlayerInteractListeners implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PlayerInteractEvent event) {
        if (!event.hasBlock() || !event.getClickedBlock().getType().equals(Material.CHEST)) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        SafeSettings settings = SafeProvider.getSettings();

        if (!settings.getAppId().equalsIgnoreCase(CoreProvider.getApp().getId())) {
            return;
        }

        Location triggerLocation = event.getClickedBlock().getLocation();

        Optional<Integer> index = settings.getTriggers().entrySet().stream()
                .filter(entry -> {

                    Location location = new Location(
                            triggerLocation.getWorld(),
                            entry.getKey().getX(),
                            entry.getKey().getY(),
                            entry.getKey().getZ()
                    );

                    return LocationUtils.compareLocation(triggerLocation, location);
                })
                .map(Map.Entry::getValue)
                .findFirst();

        if (!index.isPresent()) {
            event.getPlayer().kickPlayer(ChatColor.RED + "Você foi removido para fora do cofre.");

            return;
        }

        event.setCancelled(true);

        boolean success = SafeController.open(event.getPlayer(), index.get());

        if (!success) {
            event.getPlayer().kickPlayer(ChatColor.RED + "Você foi removido para fora do cofre.");
        }
    }
}
