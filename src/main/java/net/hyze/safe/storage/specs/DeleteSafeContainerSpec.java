package net.hyze.safe.storage.specs;

import java.sql.PreparedStatement;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.storage.repositories.specs.DeleteSqlSpec;
import org.springframework.jdbc.core.PreparedStatementCreator;

@RequiredArgsConstructor
public class DeleteSafeContainerSpec extends DeleteSqlSpec<Boolean> {

    private final int safeId, position;

    @Override
    public Boolean parser(int affectedRows) {
        return affectedRows != 0;
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            String query = String.format(
                    "DELETE FROM `safes_containers` WHERE `safe_id` = %s AND `position` = %s  LIMIT 1;",
                    safeId, position
            );

            PreparedStatement statement = connection.prepareStatement(query);

            return statement;
        };
    }

}
