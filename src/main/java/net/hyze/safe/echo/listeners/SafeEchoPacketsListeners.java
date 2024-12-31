package net.hyze.safe.echo.listeners;

import dev.utils.echo.IEchoListener;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.user.User;
import net.hyze.safe.SafeController;
import net.hyze.safe.SafeProvider;
import net.hyze.safe.echo.packets.SelectSafeEchoPacket;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.greenrobot.eventbus.Subscribe;

public class SafeEchoPacketsListeners implements IEchoListener {

    @Subscribe
    public void on(SelectSafeEchoPacket packet) {

        if (CoreProvider.getApp().isSame(SafeProvider.getSettings().getAppId())) {
            User user = CoreProvider.Cache.Local.USERS.provide().get(packet.getUserId());

            SafeController.setCurrentSafeId(user, packet.getSafeId());

            Player player = Bukkit.getPlayerExact(user.getNick());
            if (player != null && player.isOnline()) {
                player.teleport(new Location(
                        Bukkit.getWorld("world"),
                        SafeProvider.getSettings().getSpawn().getX(),
                        SafeProvider.getSettings().getSpawn().getY(),
                        SafeProvider.getSettings().getSpawn().getZ()
                ));
            } else {
                packet.setResponse(new SelectSafeEchoPacket.SelectSafeResponse());
            }
        }
    }
}

