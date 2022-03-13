package de.notion.pipeline.sql;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.notion.pipeline.Pipeline;
import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.operator.FindOptions;
import de.notion.pipeline.part.storage.GlobalStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public abstract class SqlStorage implements GlobalStorage {

    protected static final String TABLE_COLUMN_KEY = "UUID";
    protected static final String TABLE_COLUMN_VAL = "Document";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `%s` (%s VARCHAR(64) PRIMARY KEY, %s TEXT);";
    private static final String SELECT_ALL = "SELECT %s FROM `%s`;";
    private static final String SELECT_BY_UUID = "SELECT %s FROM `%s` WHERE %s = ?";
    private static final String INSERT_BY_UUID = "INSERT INTO `%s` (%s,%s) VALUES (?, ?);";
    private static final String UPDATE_BY_UUID = "UPDATE `%s` SET %s=? WHERE %s=?";
    private static final String DELETE_BY_UUID = "DELETE FROM `%s` WHERE %s = ?";

    protected final Gson gson;

    public SqlStorage(Pipeline pipeline) {
        this.gson = pipeline.gson();
    }

    @Override
    public JsonObject loadData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
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
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
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
    public void saveData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonObject dataToSave) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        Objects.requireNonNull(dataToSave, "dataToSave can't be null!");
        if (!dataExist(dataClass, objectUUID)) {
            executeUpdate(
                    String.format(INSERT_BY_UUID, tableName(dataClass), TABLE_COLUMN_KEY, TABLE_COLUMN_VAL),
                    objectUUID.toString(), gson.toJson(dataToSave)
            );
        } else {
            executeUpdate(
                    String.format(UPDATE_BY_UUID, tableName(dataClass), TABLE_COLUMN_VAL, TABLE_COLUMN_KEY),
                    gson.toJson(dataToSave), objectUUID.toString()
            );
        }
    }

    @Override
    public boolean removeData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        return executeUpdate(
                String.format(DELETE_BY_UUID, tableName(dataClass), TABLE_COLUMN_KEY),
                objectUUID.toString()
        ) != -1;
    }

    @Override
    public @NotNull List<UUID> savedUUIDs(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        return findUUIDs(dataClass, new FindOptions());
    }

    @Override
    public @NotNull List<UUID> findUUIDs(@NotNull Class<? extends PipelineData> dataClass, @NotNull FindOptions findOptions) {
        return executeQuery(
                String.format(SELECT_ALL, TABLE_COLUMN_KEY, tableName(dataClass)),
                resultSet -> {
                    var uuids = new ArrayList<UUID>();

                    var filter = findOptions.filter();
                    var skip = findOptions.skip();
                    var limit = findOptions.limit() + skip;
                    try {
                        for (int i = 0; resultSet.next(); i++) {
                            var data = resultSet.getString(TABLE_COLUMN_VAL);
                            if (skip > i)
                                continue;
                            if (i >= limit)
                                break;

                            JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();

                            if (filter != null && !filter.check(jsonObject))
                                continue;

                            uuids.add(UUID.fromString(jsonObject.getAsJsonPrimitive("objectUUID").getAsString()));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    return uuids;
                }, List.of());
    }

    private void createTableIfNotExists(@NotNull Class<? extends PipelineData> dataClass, @NotNull String name) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(name, "name can't be null!");
        executeUpdate(String.format(
                CREATE_TABLE,
                name,
                TABLE_COLUMN_KEY,
                TABLE_COLUMN_VAL
        ));
    }

    private String tableName(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        var name = AnnotationResolver.storageIdentifier(dataClass);
        createTableIfNotExists(dataClass, name);
        return name;
    }

    @NotNull
    public abstract Connection connection();

    public abstract int executeUpdate(@NotNull String query, @NotNull Object... objects);

    public abstract <T> T executeQuery(
            @NotNull String query,
            @NotNull Function<ResultSet, T> callback,
            @Nullable T def,
            @NotNull Object... objects
    );
}
