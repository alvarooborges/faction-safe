package net.hyze.safe;

import lombok.Getter;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.CustomPlugin;
import net.hyze.core.spigot.commands.CommandRegistry;
import net.hyze.core.spigot.misc.hiddencuboid.HiddenCuboidManager;
import net.hyze.core.spigot.misc.hiddencuboid.HiddenResult;
import net.hyze.safe.commands.SafeCommand;
import net.hyze.safe.echo.listeners.SafeEchoPacketsListeners;
import net.hyze.safe.listeners.PlayerGenericListeners;
import net.hyze.safe.listeners.PlayerInteractListeners;

public class SafePlugin extends CustomPlugin {

    @Getter
    private static SafePlugin instance;

    public SafePlugin() {
        super(false);
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        CommandRegistry.registerCommand(new SafeCommand());


        if (AppType.FACTIONS_SAFE.isCurrent()) {

            getServer().getPluginManager().registerEvents(new PlayerInteractListeners(), this);
            getServer().getPluginManager().registerEvents(new PlayerGenericListeners(), this);

            HiddenCuboidManager.registerHiddenBiFunction((player, target) -> {

                if (!SafeProvider.getSettings().getCuboid().contains(target.getLocation(), true)) {
                    return HiddenResult.NONE;
                }

                User user = CoreProvider.Cache.Local.USERS.provide().get(player.getName());
                User targetUser = CoreProvider.Cache.Local.USERS.provide().get(target.getName());

                Integer playerCurrentSafeId = SafeController.getCurrentSafeId(user);
                Integer targetCurrentSafeId = SafeController.getCurrentSafeId(targetUser);

                if (playerCurrentSafeId != null && playerCurrentSafeId.equals(targetCurrentSafeId)) {
                    return HiddenResult.SHOW;
                }

                return HiddenResult.HIDE;
            });

            HiddenCuboidManager.registerHiddenCuboid(SafeProvider.getSettings().getAppId(), SafeProvider.getSettings().getCuboid());
        }

        CoreProvider.Redis.ECHO.provide().registerListener(new SafeEchoPacketsListeners());
    }
}
