package de.natrox.pipeline.sqllite;

import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.sql.SqlStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

public class SqlLiteStorage extends SqlStorage {

    private final static Logger LOGGER = LogManager.logger(SqlLiteStorage.class);

    private final Connection connection;

    public SqlLiteStorage(Pipeline pipeline, Connection connection) {
        super(pipeline);
        this.connection = connection;
        LOGGER.debug("SqlLite storage started"); 
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
            LOGGER.severe("Exception while executing database update");
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
            LOGGER.severe("Exception while executing database query");
            throwable.printStackTrace();
            return null;
        }
    }
}
