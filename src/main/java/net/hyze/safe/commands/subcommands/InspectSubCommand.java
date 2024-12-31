package net.hyze.safe.commands.subcommands;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import lombok.Getter;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.commands.argument.Argument;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.commands.GroupCommandRestrictable;
import net.hyze.core.shared.commands.argument.impl.NickArgument;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.misc.utils.DefaultMessage;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.commands.CustomCommand;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.safe.Safe;
import net.hyze.safe.SafeController;
import net.hyze.safe.SafeProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class InspectSubCommand extends CustomCommand implements GroupCommandRestrictable {

    @Getter
    public Group group = Group.MANAGER;

    public InspectSubCommand() {
        super("inspect", CommandRestriction.IN_GAME);
        registerArgument(new NickArgument("nick", "", true));
        registerArgument(new Argument("id", "", true));
    }

    @Override
    public void onCommand(CommandSender sender, User user, String[] args) {
        Player player = (Player) sender;

        User target = CoreProvider.Cache.Local.USERS.provide().get(args[0]);

        if (target == null) {
            Message.ERROR.send(sender, DefaultMessage.PLAYER_NOT_FOUND.format(args[0]));
            return;
        }

        Integer safeIndex = Ints.tryParse(args[1]);

        if (safeIndex != null && safeIndex > 0) {
            Set<Safe> safes = SafeProvider.Repositories.SAFE.provide().fetchSafesIds(target);

            if (!safes.isEmpty() && safes.size() >= safeIndex) {
                SafeController.tryTeleportToSafe(
                        user,
                        player,
                        Lists.newArrayList(safes).get(safeIndex - 1).getId()
                );
                return;
            }

            Message.ERROR.send(player, "O jogador não possui o Cofre #" + safeIndex);
            return;
        }

        Message.ERROR.send(player, "Use /cofre inspect [nick] [id]");
    }
}
