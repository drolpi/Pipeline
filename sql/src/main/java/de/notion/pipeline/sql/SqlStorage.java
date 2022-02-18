package de.notion.pipeline.sql;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.filter.Filter;
import de.notion.pipeline.part.storage.GlobalStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public abstract class SqlStorage implements GlobalStorage {

    protected static final String TABLE_COLUMN_KEY = "UUID";
    protected static final String TABLE_COLUMN_VAL = "Document";
    protected static final Gson GSON = new GsonBuilder().serializeNulls().create();

    public SqlStorage() {

    }

    @Override
    public String loadData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        return executeQuery(
                String.format("SELECT %s FROM `%s` WHERE %s = ?", TABLE_COLUMN_VAL, getTableName(dataClass), TABLE_COLUMN_KEY),
                resultSet -> {
                    try {
                        return resultSet.next() ? resultSet.getString(TABLE_COLUMN_VAL) : null;
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
                String.format("SELECT %s FROM `%s` WHERE %s = ?", TABLE_COLUMN_KEY, getTableName(dataClass), TABLE_COLUMN_KEY),
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
    public void saveData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull String dataToSave) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        Objects.requireNonNull(dataToSave, "dataToSave can't be null!");
        if (!dataExist(dataClass, objectUUID)) {
            executeUpdate(
                    "INSERT INTO `" + getTableName(dataClass) + "` (" + TABLE_COLUMN_KEY + "," + TABLE_COLUMN_VAL + ") VALUES (?, ?);",
                    objectUUID.toString(), dataToSave
            );
        } else {
            executeUpdate(
                    "UPDATE `" + getTableName(dataClass) + "` SET " + TABLE_COLUMN_VAL + "=? WHERE " + TABLE_COLUMN_KEY + "=?",
                    dataToSave, objectUUID.toString()
            );
        }
    }

    @Override
    public boolean removeData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        return executeUpdate(
                String.format("DELETE FROM `%s` WHERE %s = ?", getTableName(dataClass), TABLE_COLUMN_KEY),
                objectUUID.toString()
        ) != -1;
    }

    @Override
    public Set<UUID> getSavedUUIDs(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        return executeQuery(
                String.format("SELECT %s FROM `%s`;", TABLE_COLUMN_KEY, getTableName(dataClass)),
                resultSet -> {
                    Set<UUID> keys = new HashSet<>();
                    try {
                        while (resultSet.next()) {
                            keys.add(UUID.fromString(resultSet.getString(TABLE_COLUMN_KEY)));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    return keys;
                }, new HashSet<>());
    }

    @Override
    public List<UUID> filter(@NotNull Class<? extends PipelineData> type, @NotNull Filter filter) {
        return executeQuery(
                String.format("SELECT * FROM `%s`;", getTableName(type)),
                resultSet -> {
                    List<UUID> uuids = new ArrayList<>();
                    try {
                        while (resultSet.next()) {
                            Map<String, Object> document = GSON.fromJson(resultSet.getString(TABLE_COLUMN_VAL), Map.class);

                            if (filter.check(document)) {
                                uuids.add(UUID.fromString((String) document.get("objectUUID")));
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    return uuids;
                }, new ArrayList<>());
    }

    private void createTableIfNotExists(@NotNull Class<? extends PipelineData> dataClass, @NotNull String name) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(name, "name can't be null!");
        executeUpdate(String.format(
                "CREATE TABLE IF NOT EXISTS `%s` (%s VARCHAR(64) PRIMARY KEY, %s TEXT);",
                name,
                TABLE_COLUMN_KEY,
                TABLE_COLUMN_VAL
        ));
    }

    private String getTableName(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        String name = AnnotationResolver.getStorageIdentifier(dataClass);
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
