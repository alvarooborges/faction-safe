package net.hyze.safe.storage.specs;

import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.storage.repositories.specs.DeleteSqlSpec;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.PreparedStatement;

@RequiredArgsConstructor
public class DeleteSafeSpec extends DeleteSqlSpec<Boolean> {

    private final int safeId;

    @Override
    public Boolean parser(int affectedRows) {
        return affectedRows != 0;
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            String query = String.format(
                    "DELETE FROM `safes` WHERE `id` = %s LIMIT 1;",
                    safeId
            );

            PreparedStatement statement = connection.prepareStatement(query);

            return statement;
        };
    }

}
