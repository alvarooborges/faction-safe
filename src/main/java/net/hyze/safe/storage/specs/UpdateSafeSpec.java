package net.hyze.safe.storage.specs;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.storage.repositories.specs.UpdateSqlSpec;
import net.hyze.safe.Safe;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.PreparedStatement;

@RequiredArgsConstructor
public class UpdateSafeSpec extends UpdateSqlSpec<Boolean> {

    private final Safe safe;

    @Override
    public Boolean parser(int affectedRows) {
        return affectedRows != 0;
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            String query = String.format(
                    "UPDATE `safes` SET `icon_material` = '%s', `icon_data` = '%s' " +
                            "WHERE `id` = %s LIMIT 1;",
                    safe.getIconMaterial().name(),
                    safe.getMaterialData(),
                    safe.getId()
            );

            PreparedStatement statement = connection.prepareStatement(query);

            return statement;
        };
    }
}
