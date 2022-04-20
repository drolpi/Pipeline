/*
 * Copyright 2020-2022 NatroxMC team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.natrox.pipeline.sql;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.function.ThrowableFunction;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.part.storage.GlobalStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public abstract class SqlStorage implements GlobalStorage {

    private final static Logger LOGGER = LoggerFactory.getLogger(SqlStorage.class);

    private static final String TABLE_COLUMN_KEY = "UUID";
    private static final String TABLE_COLUMN_VAL = "Document";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `%s` (%s VARCHAR(64) PRIMARY KEY, %s TEXT);";
    private static final String SELECT_ALL = "SELECT %s FROM `%s`;";
    private static final String SELECT_BY_UUID = "SELECT %s FROM `%s` WHERE %s = ?";
    private static final String INSERT_BY_UUID = "INSERT INTO `%s` (%s,%s) VALUES (?, ?);";
    private static final String UPDATE_BY_UUID = "UPDATE `%s` SET %s=? WHERE %s=?";
    private static final String DELETE_BY_UUID = "DELETE FROM `%s` WHERE %s = ?";

    private final JsonDocument.Factory documentFactory;
    private final HikariDataSource hikariDataSource;

    public SqlStorage(Pipeline pipeline, HikariDataSource hikariDataSource) {
        this.hikariDataSource = hikariDataSource;
        this.documentFactory = pipeline.documentFactory();
    }

    @Override
    public JsonDocument get(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        return executeQuery(
            String.format(SELECT_BY_UUID, TABLE_COLUMN_VAL, tableName(dataClass), TABLE_COLUMN_KEY),
            resultSet -> resultSet.next() ? documentFactory.fromJsonString(resultSet.getString(TABLE_COLUMN_VAL)) : null,
            null,
            objectUUID.toString()
        );
    }

    @Override
    public boolean exists(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        return executeQuery(
            String.format(SELECT_BY_UUID, TABLE_COLUMN_KEY, tableName(dataClass), TABLE_COLUMN_KEY),
            ResultSet::next,
            false,
            objectUUID.toString()
        );
    }

    @Override
    public void save(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonDocument data) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        Preconditions.checkNotNull(data, "data");

        if (!exists(dataClass, objectUUID)) {
            executeUpdate(
                String.format(INSERT_BY_UUID, tableName(dataClass), TABLE_COLUMN_KEY, TABLE_COLUMN_VAL),
                objectUUID.toString(), data.toString()
            );
        } else {
            executeUpdate(
                String.format(UPDATE_BY_UUID, tableName(dataClass), TABLE_COLUMN_VAL, TABLE_COLUMN_KEY),
                data.toString(), objectUUID.toString()
            );
        }
    }

    @Override
    public boolean remove(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        return executeUpdate(
            String.format(DELETE_BY_UUID, tableName(dataClass), TABLE_COLUMN_KEY),
            objectUUID.toString()
        ) != -1;
    }

    @Override
    public @NotNull Collection<UUID> keys(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        return executeQuery(
            String.format(SELECT_ALL, TABLE_COLUMN_KEY, tableName(dataClass)),
            resultSet -> {
                Collection<UUID> keys = new ArrayList<>();
                while (resultSet.next()) {
                    keys.add(UUID.fromString(resultSet.getString(TABLE_COLUMN_KEY)));
                }
                return keys;
            }, List.of());
    }

    @Override
    public @NotNull Collection<JsonDocument> documents(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        return executeQuery(
            String.format(SELECT_ALL, TABLE_COLUMN_VAL, tableName(dataClass)),
            resultSet -> {
                Collection<JsonDocument> documents = new ArrayList<>();
                while (resultSet.next()) {
                    documents.add(documentFactory.fromJsonString(resultSet.getString(TABLE_COLUMN_VAL)));
                }
                return documents;
            }, List.of());
    }

    @Override
    public @NotNull Map<UUID, JsonDocument> entries(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        return executeQuery(
            String.format(SELECT_ALL, tableName(dataClass)),
            resultSet -> {
                Map<UUID, JsonDocument> map = new HashMap<>();
                while (resultSet.next()) {
                    map.put(
                        UUID.fromString(resultSet.getString(TABLE_COLUMN_KEY)),
                        documentFactory.fromJsonString(resultSet.getString(TABLE_COLUMN_VAL))
                    );
                }
                return map;
            }, Map.of());
    }

    @Override
    public @NotNull Map<UUID, JsonDocument> filter(@NotNull Class<? extends PipelineData> dataClass, @NotNull BiPredicate<UUID, JsonDocument> predicate) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(predicate, "predicate");
        return executeQuery(
            String.format(SELECT_ALL, tableName(dataClass)),
            resultSet -> {
                Map<UUID, JsonDocument> map = new HashMap<>();
                while (resultSet.next()) {
                    var key = UUID.fromString(resultSet.getString(TABLE_COLUMN_KEY));
                    var document = documentFactory.fromJsonString(resultSet.getString(TABLE_COLUMN_VAL));

                    if (predicate.test(key, document)) {
                        map.put(key, document);
                    }
                }
                return map;
            }, Map.of());
    }

    @Override
    public void iterate(@NotNull Class<? extends PipelineData> dataClass, @NotNull BiConsumer<UUID, JsonDocument> consumer) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(consumer, "consumer");
        executeQuery(
            String.format(SELECT_ALL, tableName(dataClass)),
            resultSet -> {
                while (resultSet.next()) {
                    var key = UUID.fromString(resultSet.getString(TABLE_COLUMN_KEY));
                    var document = documentFactory.fromJsonString(resultSet.getString(TABLE_COLUMN_VAL));
                    consumer.accept(key, document);
                }
                return null;
            }, null);
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
            LOGGER.error("Exception while executing database update");
            exception.printStackTrace();
            return -1;
        }
    }

    public <T> T executeQuery(@NotNull String query, @NotNull ThrowableFunction<ResultSet, T, SQLException> callback, @Nullable T def, @NotNull Object... objects) {
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
            LOGGER.error("Exception while executing database query");
            throwable.printStackTrace();
        }

        return def;
    }
}
