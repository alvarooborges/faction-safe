package net.hyze.safe.commands;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.commands.CustomCommand;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.safe.Safe;
import net.hyze.safe.SafeController;
import net.hyze.safe.SafeProvider;
import net.hyze.safe.commands.subcommands.ExitSubCommand;
import net.hyze.safe.commands.subcommands.InspectSubCommand;
import net.hyze.safe.commands.subcommands.InviteSubCommand;
import net.hyze.safe.inventories.MainSafeInventory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

public class SafeCommand extends CustomCommand {

    public SafeCommand() {
        super("cofre", CommandRestriction.IN_GAME);

        if (SafeProvider.getSettings().isAllowFriend()) {
            registerSubCommand(new InviteSubCommand());
        }

        registerSubCommand(new ExitSubCommand());
        registerSubCommand(new InspectSubCommand());
    }

    @Override
    public void onCommand(CommandSender sender, User user, String[] args) {
        Player player = (Player) sender;

        if (!user.hasGroup(Group.MANAGER)) {
            Map<String, String> settings = CoreProvider.Cache.Redis.SETTINGS.provide()
                    .fetchSettingsByServer(CoreProvider.getApp().getServer());

            String value;
            if ((value = settings.get("safe_status")) != null && value.equalsIgnoreCase("false")) {
                Message.ERROR.send(player, "Os cofres estão temporariamente desativados.");
                return;
            }
        }

        if (args.length > 0) {
            Integer safeIndex = Ints.tryParse(args[0]);

            if (safeIndex != null && safeIndex > 0) {
                Set<Safe> safes = SafeProvider.Repositories.SAFE.provide().fetchSafesIds(user);

                if (!safes.isEmpty() && safes.size() >= safeIndex) {
                    SafeController.tryTeleportToSafe(
                            user,
                            player,
                            Lists.newArrayList(safes).get(safeIndex - 1).getId()
                    );
                    return;
                }

                Message.ERROR.send(player, "Você não possui o Cofre #" + safeIndex);
                return;
            }
        }

        player.closeInventory();
        player.openInventory(new MainSafeInventory(user, player));
    }
}
