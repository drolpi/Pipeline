package de.notion.pipeline.sql.mysql;

import com.zaxxer.hikari.HikariDataSource;
import de.notion.pipeline.sql.SqlStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Function;

public class MySqlStorage extends SqlStorage {

    private final HikariDataSource hikariDataSource;

    public MySqlStorage(HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
    }

    @NotNull
    @Override
    public Connection connection() {
        try {
            return this.hikariDataSource.getConnection();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to retrieve connection from pool", exception);
        }
    }

    @Override
    public int executeUpdate(@NotNull String query, @NotNull Object... objects) {
        try (Connection con = this.connection(); PreparedStatement statement = con.prepareStatement(query)) {
            // write all parameters
            for (int i = 0; i < objects.length; i++) {
                statement.setString(i + 1, Objects.toString(objects[i]));
            }

            // execute the statement
            return statement.executeUpdate();
        } catch (SQLException exception) {
            System.out.println("Exception while executing database update");
            exception.printStackTrace();
            return -1;
        }
    }

    @Override
    public <T> T executeQuery(@NotNull String query, @NotNull Function<ResultSet, T> callback, @Nullable T def, @NotNull Object... objects) {
        try (Connection con = this.connection(); PreparedStatement statement = con.prepareStatement(query)) {
            // write all parameters
            for (int i = 0; i < objects.length; i++) {
                statement.setString(i + 1, Objects.toString(objects[i]));
            }

            // execute the statement, apply to the result handler
            try (ResultSet resultSet = statement.executeQuery()) {
                return callback.apply(resultSet);
            }
        } catch (Throwable throwable) {
            System.out.println("Exception while executing database query");
            throwable.printStackTrace();
        }

        return def;
    }
}
