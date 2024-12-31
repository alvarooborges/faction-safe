package net.hyze.safe.listeners;

import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.echo.packets.SendMessagePacket;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.connect.events.AsyncUserConnectHandShakeEvent;
import net.hyze.core.spigot.misc.combat.CombatManager;
import net.hyze.core.spigot.misc.utils.LocationUtils;
import net.hyze.safe.SafeController;
import net.hyze.safe.SafeProvider;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.Collections;

public class PlayerGenericListeners implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerDropItemEvent event) {
        if (CoreProvider.getApp().isSame(SafeProvider.getSettings().getAppId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerQuitEvent event) {
        if (CoreProvider.getApp().isSame(SafeProvider.getSettings().getAppId())) {
            User user = CoreProvider.Cache.Local.USERS.provide().get(event.getPlayer().getName());
            SafeController.setCurrentSafeId(user, null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(AsyncUserConnectHandShakeEvent event) {
        if (CoreProvider.getApp().isSame(SafeProvider.getSettings().getAppId())) {
            Integer id = SafeController.getCurrentSafeId(event.getUser());

            if (id == null) {
                event.setCancelled(true);
                CoreProvider.Redis.ECHO.provide().publish(new SendMessagePacket(
                        Collections.singleton(event.getUser().getId()),
                        TextComponent.fromLegacyText(ChatColor.RED + "Use /cofre para entrar em um cofre.")
                ));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(AsyncPlayerPreLoginEvent event) {
        if (CoreProvider.getApp().isSame(SafeProvider.getSettings().getAppId())) {
            User user = CoreProvider.Cache.Local.USERS.provide().get(event.getName());
            Integer id = SafeController.getCurrentSafeId(user);

            if (id == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Use /cofre para entrar em um cofre.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PlayerInitialSpawnEvent event) {
        if (CoreProvider.getApp().isSame(SafeProvider.getSettings().getAppId())) {
            event.setSpawnLocation(new Location(
                    Bukkit.getWorld("world"),
                    SafeProvider.getSettings().getSpawn().getX(),
                    SafeProvider.getSettings().getSpawn().getY(),
                    SafeProvider.getSettings().getSpawn().getZ()
            ));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(PlayerMoveEvent event) {
        if (CoreProvider.getApp().isSame(SafeProvider.getSettings().getAppId())) {

            if (LocationUtils.compareLocation(event.getTo(), event.getFrom())) {
                return;
            }

            User user = CoreProvider.Cache.Local.USERS.provide().get(event.getPlayer().getName());
            Integer id = SafeController.getCurrentSafeId(user);

            if (id == null) {

                CombatManager.untag(user);
                event.getPlayer().kickPlayer(ChatColor.RED + "Você não pode ficar fora do cofre.");

            } else if (!SafeProvider.getSettings().getCuboid().contains(event.getTo(), true)) {

                event.getPlayer().teleport(new Location(
                        Bukkit.getWorld("world"),
                        SafeProvider.getSettings().getSpawn().getX(),
                        SafeProvider.getSettings().getSpawn().getY(),
                        SafeProvider.getSettings().getSpawn().getZ()
                ));

            }
        }
    }
}
