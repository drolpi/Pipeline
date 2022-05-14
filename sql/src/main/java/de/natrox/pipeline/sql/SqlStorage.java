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

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.common.function.ThrowableFunction;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.part.PartMap;
import de.natrox.pipeline.part.storage.GlobalStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SqlStorage implements GlobalStorage {

    private final JsonConverter jsonConverter;
    private final HikariDataSource dataSource;
    private final Map<String, SqlMap> sqlMapRegistry;

    public SqlStorage(Pipeline pipeline, HikariDataSource dataSource) {
        this.jsonConverter = pipeline.jsonConverter();
        this.dataSource = dataSource;
        this.sqlMapRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public @NotNull PartMap openMap(@NotNull String mapName) {
        if (sqlMapRegistry.containsKey(mapName)) {
            return sqlMapRegistry.get(mapName);
        }
        SqlMap sqlMap = new SqlMap(this, mapName, jsonConverter);
        sqlMapRegistry.put(mapName, sqlMap);

        return sqlMap;
    }

    @Override
    public boolean hasMap(@NotNull String mapName) {
        return false;
    }

    @Override
    public void closeMap(@NotNull String mapName) {

    }

    @Override
    public void removeMap(@NotNull String mapName) {

    }

    private void createTableIfNotExists(@NotNull String name) {
        Check.notNull(name, "name");
        executeUpdate(String.format(
            SQLConstants.CREATE_TABLE,
            name,
            SQLConstants.TABLE_COLUMN_KEY,
            SQLConstants.TABLE_COLUMN_VAL
        ));
    }

    @NotNull
    public Connection connection() {
        try {
            return this.dataSource.getConnection();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to retrieve connection from pool", exception);
        }
    }

    public int executeUpdate(@NotNull String query, @NotNull Object... objects) {
        try (Connection con = this.connection(); PreparedStatement statement = con.prepareStatement(query)) {
            // write all parameters
            for (int i = 0; i < objects.length; i++) {
                statement.setString(i + 1, Objects.toString(objects[i]));
            }

            // execute the statement
            return statement.executeUpdate();
        } catch (SQLException exception) {
            //LOGGER.error("Exception while executing database update");
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
            //LOGGER.error("Exception while executing database query");
            throwable.printStackTrace();
        }

        return def;
    }
}
