package de.natrox.pipeline.sql;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.part.storage.GlobalStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public abstract class SqlStorage implements GlobalStorage {

    private final static Logger LOGGER = LogManager.logger(SqlStorage.class);

    private static final String TABLE_COLUMN_KEY = "UUID";
    private static final String TABLE_COLUMN_VAL = "Document";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `%s` (%s VARCHAR(64) PRIMARY KEY, %s TEXT);";
    private static final String SELECT_ALL = "SELECT %s FROM `%s`;";
    private static final String SELECT_BY_UUID = "SELECT %s FROM `%s` WHERE %s = ?";
    private static final String INSERT_BY_UUID = "INSERT INTO `%s` (%s,%s) VALUES (?, ?);";
    private static final String UPDATE_BY_UUID = "UPDATE `%s` SET %s=? WHERE %s=?";
    private static final String DELETE_BY_UUID = "DELETE FROM `%s` WHERE %s = ?";

    private final Gson gson;
    private final HikariDataSource hikariDataSource;

    public SqlStorage(Pipeline pipeline, HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
        this.gson = pipeline.gson();
    }

    @Override
    public JsonObject loadData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        return executeQuery(
            String.format(SELECT_BY_UUID, TABLE_COLUMN_VAL, tableName(dataClass), TABLE_COLUMN_KEY),
            resultSet -> {
                try {
                    return resultSet.next() ? JsonParser.parseString(resultSet.getString(TABLE_COLUMN_VAL)).getAsJsonObject() : null;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            },
            null,
            objectUUID.toString()
        );
    }

    @Override
    public boolean dataExist(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        return executeQuery(
            String.format(SELECT_BY_UUID, TABLE_COLUMN_KEY, tableName(dataClass), TABLE_COLUMN_KEY),
            resultSet -> {
                try {
                    return resultSet.next();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            },
            false,
            objectUUID.toString());
    }

    @Override
    public void saveData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonObject data) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        Preconditions.checkNotNull(data, "data");

        if (!dataExist(dataClass, objectUUID)) {
            executeUpdate(
                String.format(INSERT_BY_UUID, tableName(dataClass), TABLE_COLUMN_KEY, TABLE_COLUMN_VAL),
                objectUUID.toString(), gson.toJson(data)
            );
        } else {
            executeUpdate(
                String.format(UPDATE_BY_UUID, tableName(dataClass), TABLE_COLUMN_VAL, TABLE_COLUMN_KEY),
                gson.toJson(data), objectUUID.toString()
            );
        }
    }

    @Override
    public boolean removeData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        return executeUpdate(
            String.format(DELETE_BY_UUID, tableName(dataClass), TABLE_COLUMN_KEY),
            objectUUID.toString()
        ) != -1;
    }

    @Override
    public @NotNull Collection<UUID> savedUUIDs(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        return data(dataClass).keySet();
    }

    @Override
    public @NotNull Map<UUID, JsonObject> data(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        return executeQuery(
            String.format(SELECT_ALL, TABLE_COLUMN_KEY, tableName(dataClass)),
            resultSet -> {
                var uuids = new HashMap<UUID, JsonObject>();

                try {
                    while (resultSet.next()) {
                        var data = resultSet.getString(TABLE_COLUMN_VAL);

                        var jsonObject = JsonParser.parseString(data).getAsJsonObject();
                        uuids.put(UUID.fromString(jsonObject.getAsJsonPrimitive("objectUUID").getAsString()), jsonObject);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return uuids;
            }, Map.of());
    }

    private void createTableIfNotExists(@NotNull Class<? extends PipelineData> dataClass, @NotNull String name) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(name, "name");

        executeUpdate(String.format(
            CREATE_TABLE,
            name,
            TABLE_COLUMN_KEY,
            TABLE_COLUMN_VAL
        ));
    }

    private String tableName(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        var name = AnnotationResolver.storageIdentifier(dataClass);
        createTableIfNotExists(dataClass, name);
        return name;
    }

    @NotNull
    public Connection connection() {
        try {
            return this.hikariDataSource.getConnection();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to retrieve connection from pool", exception);
        }
    }

    public int executeUpdate(@NotNull String query, @NotNull Object... objects) {
        try (var con = this.connection(); PreparedStatement statement = con.prepareStatement(query)) {
            // write all parameters
            for (int i = 0; i < objects.length; i++) {
                statement.setString(i + 1, Objects.toString(objects[i]));
            }

            // execute the statement
            return statement.executeUpdate();
        } catch (SQLException exception) {
            LOGGER.severe("Exception while executing database update");
            exception.printStackTrace();
            return -1;
        }
    }

    public <T> T executeQuery(@NotNull String query, @NotNull Function<ResultSet, T> callback, @Nullable T def, @NotNull Object... objects) {
        try (var con = this.connection(); PreparedStatement statement = con.prepareStatement(query)) {
            // write all parameters
            for (int i = 0; i < objects.length; i++) {
                statement.setString(i + 1, Objects.toString(objects[i]));
            }

            // execute the statement, apply to the result handler
            try (var resultSet = statement.executeQuery()) {
                return callback.apply(resultSet);
            }
        } catch (Throwable throwable) {
            LOGGER.severe("Exception while executing database query");
            throwable.printStackTrace();
        }

        return def;
    }
}
