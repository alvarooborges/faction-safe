package net.hyze.safe.storage.specs;

import com.google.common.base.Enums;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.storage.repositories.specs.SelectSqlSpec;
import net.hyze.core.shared.user.User;
import net.hyze.safe.Safe;
import org.bukkit.Material;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.PreparedStatement;
import java.util.Set;

@RequiredArgsConstructor
public class SelectSafesIdsByUserSpec extends SelectSqlSpec<Set<Safe>> {

    private final User user;

    @Override
    public ResultSetExtractor<Set<Safe>> getResultSetExtractor() {
        return result -> {
            Set<Safe> out = Sets.newLinkedHashSet();

            while (result.next()) {
                Material iconMaterial = Enums.getIfPresent(Material.class, result.getString("icon_material"))
                        .or(Material.CHEST);

                out.add(new Safe(
                        result.getInt("safe_id"),
                        iconMaterial,
                        result.getInt("icon_data")
                ));
            }

            return out;
        };
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            String query = String.format(
                    "SELECT `safes_users`.`safe_id`, `safes`.`icon_material`, `safes`.`icon_data` FROM `safes_users` " +
                            "JOIN `safes` ON `safes`.`id` = `safes_users`.`safe_id` " +
                            "WHERE `safes_users`.`user_id` = %s ORDER BY `safes`.`id`;",
                    this.user.getId()
            );

            PreparedStatement statement = connection.prepareStatement(query);

            return statement;
        };
    }
}
