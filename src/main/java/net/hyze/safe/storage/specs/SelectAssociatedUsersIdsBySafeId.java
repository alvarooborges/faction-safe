package net.hyze.safe.storage.specs;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import net.hyze.core.shared.storage.repositories.specs.SelectSqlSpec;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.PreparedStatement;
import java.util.Set;

@RequiredArgsConstructor
public class SelectAssociatedUsersIdsBySafeId  extends SelectSqlSpec<Set<Integer>> {

    private final Integer safeId;

    @Override
    public ResultSetExtractor<Set<Integer>> getResultSetExtractor() {
        return result -> {
            Set<Integer> out = Sets.newHashSet();

            while (result.next()) {
                out.add(result.getInt("user_id"));
            }

            return out;
        };
    }

    @Override
    public PreparedStatementCreator getPreparedStatementCreator() {
        return connection -> {
            String query = String.format(
                    "SELECT * FROM `safes_users` WHERE `safe_id` = %s;",
                    this.safeId
            );

            PreparedStatement statement = connection.prepareStatement(query);

            return statement;
        };
    }
}
