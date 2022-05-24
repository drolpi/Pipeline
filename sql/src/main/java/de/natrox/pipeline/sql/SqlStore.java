/*
 * Copyright 2020-2022 NatroxMC
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
import de.natrox.pipeline.mapper.Mapper;
import de.natrox.pipeline.part.AbstractStore;
import de.natrox.pipeline.part.StoreMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class SqlStore extends AbstractStore {

    private final Mapper mapper;
    private final HikariDataSource dataSource;

    public SqlStore(Pipeline pipeline, HikariDataSource dataSource) {
        this.mapper = pipeline.mapper();
        this.dataSource = dataSource;
    }

    @Override
    protected StoreMap createMap(@NotNull String mapName) {
        return new SqlMap(this, mapName, this.mapper);
    }

    @Override
    public @NotNull Set<String> maps() {
        //TODO: test
        return this.executeQuery(
            String.format(SQLConstants.SELECT_BY, SQLConstants.TABLE_NAME, "INFORMATION_SCHEMA.TABLES", "TABLE_SCHEMA"),
            resultSet -> {
                Set<String> names = new HashSet<>();
                while (resultSet.next())
                    names.add(resultSet.getString(SQLConstants.TABLE_NAME));

                return names;
            },
            Set.of(),
            "test"
        );
    }

    @Override
    public boolean hasMap(@NotNull String mapName) {
        return false;
    }

    @Override
    public void removeMap(@NotNull String mapName) {

    }

    @Override
    public boolean isClosed() {
        //TODO:
        return false;
    }

    @Override
    public void close() {
        //TODO:
    }

    private void createTableIfNotExists(@NotNull String name) {
        Check.notNull(name, "name");
        executeUpdate(String.format(
            SQLConstants.CREATE_TABLE,
            name,
            SQLConstants.COLUMN_KEY,
            SQLConstants.COLUMN_VAL
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
