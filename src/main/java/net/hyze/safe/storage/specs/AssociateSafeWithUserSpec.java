package net.hyze.safe.storage.specs;

import java.sql.PreparedStatement;
import java.sql.Statement;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.storage.repositories.specs.InsertSqlSpec;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.KeyHolder;

@RequiredArgsConstructor
public class AssociateSafeWithUserSpec extends InsertSqlSpec<Boolean> {

    private final int safeId, userId;

    @Override
    public Boolean parser(int affectedRows, KeyHolder keyHolder) {
        return affectedRows != 0;
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            String query = String.format(
                    "INSERT INTO `safes_users` (`safe_id`, `user_id`) VALUES (%s, %s);",
                    safeId,
                    userId
            );

            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            return statement;
        };
    }
}
