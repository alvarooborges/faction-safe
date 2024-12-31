package net.hyze.safe.storage.specs;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.storage.repositories.specs.SelectSqlSpec;
import net.hyze.core.spigot.misc.utils.InventoryUtils;
import org.bukkit.inventory.ItemStack;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import dev.utils.shared.Printer;

import java.sql.PreparedStatement;
import java.util.Map;

@RequiredArgsConstructor
public class SelectSafeContainersBySafeIdSpec extends SelectSqlSpec<Map<Integer, ItemStack[]>> {

    private final Integer safeId;

    @Override
    public ResultSetExtractor<Map<Integer, ItemStack[]>> getResultSetExtractor() {
        return result -> {
            Map<Integer, ItemStack[]> out = Maps.newHashMap();

            while (result.next()) {
                try {
                    out.put(result.getInt("position"), InventoryUtils.deserializeContents(result.getString("serialized_inventory")));
                } catch (Exception e) {
                    e.printStackTrace();
                    Printer.ERROR.print(safeId, result.getInt("position"), result.getString("serialized_inventory"));
                }
            }

            return out;
        };
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            String query = String.format(
                    "SELECT * FROM `safes_containers` WHERE `safe_id` = %s;",
                    this.safeId
            );

            PreparedStatement statement = connection.prepareStatement(query);

            return statement;
        };
    }
}
