package net.hyze.safe;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.apps.AppType;
import net.hyze.core.shared.echo.packets.user.connect.ConnectReason;
import net.hyze.core.shared.group.Group;
import net.hyze.core.shared.messages.MessageUtils;
import net.hyze.core.shared.user.User;
import net.hyze.core.spigot.inventory.CustomInventory;
import net.hyze.core.spigot.misc.message.Message;
import net.hyze.core.spigot.misc.utils.teleporter.Teleporter;
import net.hyze.safe.echo.packets.SelectSafeEchoPacket;
import net.hyze.safe.inventories.SafeContainerInventory;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SafeController {

    /**
     * User ID -> Safe ID
     */
    private static final Map<Integer, Integer> CURRENT_SAFE = Maps.newHashMap();

    /**
     * Safe ID -> Index -> Inventory
     */
    private static final Table<Integer, Integer, CustomInventory> INVENTORIES = HashBasedTable.create();

    public static void setCurrentSafeId(User user, Integer safeId) {
        if (safeId != null && safeId != 0) {
            CURRENT_SAFE.put(user.getId(), safeId);
        } else {
            CURRENT_SAFE.remove(user.getId());
        }
    }

    public static Integer getCurrentSafeId(User user) {
        return CURRENT_SAFE.get(user.getId());
    }

    public static void tryTeleportToSafe(User user, Player player, int safeId) {
        App app = CoreProvider.Cache.Local.APPS.provide().get(SafeProvider.getSettings().getAppId());

        if (app == null) {
            player.closeInventory();
            Message.ERROR.send(player, "Os cofres estão indisponiveis no momento.");
            return;
        }

        SelectSafeEchoPacket selectSafeEchoPacket = new SelectSafeEchoPacket(user.getId(), safeId);

        Message.EMPTY.send(player, "&8Buscando cofre...");
        CoreProvider.Redis.ECHO.provide().publish(selectSafeEchoPacket, app.getId(), response -> {
            Message.EMPTY.send(player, "&8Indo para o cofre...");

            Teleporter.builder()
                    .toAppType(AppType.FACTIONS_SAFE)
                    .reason(ConnectReason.WARP)
                    .welcomeMessage(TextComponent.fromLegacyText(MessageUtils.translateFormat(
                            "&aVocê está em seu cofre. Use &f/spawn &apara sair dele."
                    )))
                    .build()
                    .teleport(user);

        });
    }

    public static boolean open(Player player, Integer position) {
        User user = CoreProvider.Cache.Local.USERS.provide().get(player.getName());

        Integer currentSafeId = CURRENT_SAFE.get(user.getId());

        if (currentSafeId == null) {
            return false;
        }

        Set<Safe> safes = SafeProvider.Repositories.SAFE.provide().fetchSafesIds(user);

        if (!user.hasGroup(Group.MANAGER)) {
            boolean hasSafe = safes.stream()
                    .map(Safe::getId)
                    .anyMatch(safeID -> Objects.equals(currentSafeId, safeID));

            if (!hasSafe) {
                return false;
            }
        }

        CustomInventory inventory = INVENTORIES.get(currentSafeId, position);

        if (inventory == null || inventory.getViewers().isEmpty()) {
            Map<Integer, ItemStack[]> containers = SafeProvider.Repositories.SAFE.provide().fetchSafeContainers(currentSafeId);

            ItemStack[] contents = new ItemStack[54];

            if (containers.containsKey(position)) {
                contents = containers.get(position);
            }

            inventory = new SafeContainerInventory(currentSafeId, position, contents);
            INVENTORIES.put(currentSafeId, position, inventory);
        }

        Lists.newArrayList(inventory.getViewers()).forEach(human -> {
            Message.ERROR.send(human, "O baú foi aberto por outro jogador.");
            human.closeInventory();
        });

        player.closeInventory();
        player.openInventory(inventory);
        return true;
    }
}
