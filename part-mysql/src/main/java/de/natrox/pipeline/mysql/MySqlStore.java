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

package de.natrox.pipeline.mysql;

import com.zaxxer.hikari.HikariDataSource;
import de.natrox.pipeline.sql.SqlStore;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

final class MySqlStore extends SqlStore {

    private final String databaseName;

    MySqlStore(HikariDataSource dataSource, String databaseName) {
        super(dataSource);
        this.databaseName = databaseName;
    }

    @Override
    public @NotNull Set<String> maps() {
        return this.executeQuery(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?",
            resultSet -> {
                Set<String> names = new HashSet<>();
                while (resultSet.next())
                    names.add(resultSet.getString("TABLE_NAME"));

                return names;
            },
            Set.of(),
            this.databaseName
        );
    }

    @Override
    public boolean hasMap(@NotNull String mapName) {
        return this.executeQuery(
            "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?",
            ResultSet::next,
            false,
            mapName
        );
    }

    @Override
    public void removeMap(@NotNull String mapName) {
        this.executeUpdate("DROP TABLE " + mapName);
        this.storeMapRegistry.remove(mapName);
    }
}
