package de.notion.pipeline.sql.sqllite;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.sql.SqlStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

public class SqlLiteStorage extends SqlStorage {

    private final Connection connection;

    public SqlLiteStorage(Pipeline pipeline, Connection connection) {
        super(pipeline);
        this.connection = connection;
        System.out.println("SqlLite storage started"); //DEBUG
    }

    @NotNull
    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public int executeUpdate(@NotNull String query, @NotNull Object... objects) {
        try (var preparedStatement = this.connection().prepareStatement(query)) {
            for (int i = 0; i < objects.length; i++) {
                preparedStatement.setString(i + 1, objects[i].toString());
            }

            return preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            System.out.println("Exception while executing database update");
            exception.printStackTrace();
            return -1;
        }
    }

    @Override
    public <T> T executeQuery(@NotNull String query, @NotNull Function<ResultSet, T> callback, @Nullable T def, @NotNull Object... objects) {
        try (var preparedStatement = this.connection().prepareStatement(query)) {
            for (int i = 0; i < objects.length; i++) {
                preparedStatement.setString(i + 1, objects[i].toString());
            }

            try (var resultSet = preparedStatement.executeQuery()) {
                return callback.apply(resultSet);
            }
        } catch (Throwable throwable) {
            System.out.println("Exception while executing database query");
            throwable.printStackTrace();
            return null;
        }
    }
}