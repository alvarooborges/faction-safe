package net.hyze.safe.storage.specs;

import java.sql.PreparedStatement;
import java.sql.Statement;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.storage.repositories.specs.InsertSqlSpec;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.KeyHolder;

@RequiredArgsConstructor
public class CreateSafeSpec extends InsertSqlSpec<Integer> {

    @Override
    public Integer parser(int affectedRows, KeyHolder keyHolder) {
        return keyHolder.getKey().intValue();
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            String query = String.format(
                    "INSERT INTO `safes` () VALUES();"
            );

            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            return statement;
        };
    }
}
