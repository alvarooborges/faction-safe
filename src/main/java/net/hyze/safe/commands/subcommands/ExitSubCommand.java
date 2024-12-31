package net.hyze.safe.commands.subcommands;

import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.commands.CommandRestriction;
import net.hyze.core.shared.echo.packets.user.connect.ConnectReason;
import net.hyze.core.shared.messages.MessageUtils;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.commands.CustomCommand;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.teleporter.Teleporter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ExitSubCommand extends CustomCommand {

    public ExitSubCommand() {
        super("sair", CommandRestriction.IN_GAME);
    }

    @Override
    public void onCommand(CommandSender sender, User user, String[] args) {
        Player player = (Player) sender;

        if (!AppType.FACTIONS_SAFE.isCurrent()) {
            Message.ERROR.send(player, "Você precisa estar dentro de um cofre.");
            return;
        }

        Teleporter.builder()
                .toAppType(AppType.FACTIONS_SPAWN)
                .reason(ConnectReason.PLUGIN)
                .welcomeMessage(TextComponent.fromLegacyText(MessageUtils.translateFormat(
                        "&aVocê saiu do cofre."
                )))
                .build()
                .teleport(user);
    }
}
