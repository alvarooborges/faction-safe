package net.hyze.safe.storage.specs;

import java.sql.PreparedStatement;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.storage.repositories.specs.UpdateSqlSpec;
import net.hyze.core.spigot.misc.utils.InventoryUtils;
import org.bukkit.inventory.ItemStack;
import org.springframework.jdbc.core.PreparedStatementCreator;

@RequiredArgsConstructor
public class SaveSafeContainerSpec extends UpdateSqlSpec<Boolean> {

    private final int safeId, position;
    private final ItemStack[] contents;

    @Override
    public Boolean parser(int affectedRows) {
        return affectedRows != 0;
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            String query = String.format(
                    "INSERT INTO `safes_containers` (`safe_id`, `position`, `serialized_inventory`) "
                    + "VALUES (%s, %s, '%s') "
                    + "ON DUPLICATE KEY UPDATE "
                    + "`serialized_inventory` = VALUES(`serialized_inventory`);",
                    safeId, position, InventoryUtils.serializeContents(contents)
            );

            PreparedStatement statement = connection.prepareStatement(query);

            return statement;
        };
    }

}
