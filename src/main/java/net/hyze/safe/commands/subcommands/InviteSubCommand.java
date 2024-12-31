package net.hyze.safe.commands.subcommands;

import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.commands.argument.impl.NickArgument;
import net.hyze.core.shared.misc.utils.DefaultMessage;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.commands.CustomCommand;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.safe.SafeController;
import net.hyze.safe.SafeProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class InviteSubCommand extends CustomCommand {

    public InviteSubCommand() {
        super("convidar", CommandRestriction.IN_GAME);

        registerArgument(new NickArgument("jogador", "", true));
    }

    @Override
    public void onCommand(CommandSender sender, User user, String[] args) {
        Player player = (Player) sender;

        User target = CoreProvider.Cache.Local.USERS.provide().get(args[0]);
        Integer currentSafeId = SafeController.getCurrentSafeId(user);

        if (!SafeProvider.getSettings().getCuboid().contains(player.getLocation(), true) || currentSafeId == null) {
            Message.ERROR.send(player, "Você precisa estar dentro de um cofre.");
            return;
        }

        if (target == null) {
            Message.ERROR.send(player, DefaultMessage.PLAYER_NOT_FOUND.format(args[0]));
            return;
        }

        if (target.equals(user)) {
            Message.ERROR.send(player, " BEEEH! Você não pode se convidar pra fazer parte de uma coisa que vc já faz parte.");
            return;
        }

        Set<Integer> usersIds = SafeProvider.Repositories.SAFE.provide().fetchAssociatedUsersIds(currentSafeId);

        if (usersIds.contains(target.getId())) {
            Message.ERROR.send(player, String.format("O jogador %s já faz parte deste cofre.", target.getNick()));
            return;
        }

        SafeProvider.Cache.Redis.INVITES.provide().createInvite(user.getId(), target.getId(), currentSafeId);
        Message.SUCCESS.send(player, "Você enviou um convite para " + target.getNick());
    }
}
