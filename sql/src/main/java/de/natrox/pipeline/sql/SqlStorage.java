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
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.part.PartMap;
import de.natrox.pipeline.part.storage.GlobalStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
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
        SqlMap sqlMap = new SqlMap();
        sqlMapRegistry.put(mapName, sqlMap);

        return sqlMap;
    }

    @Override
    public boolean hasMap(String mapName) {
        return false;
    }

    @Override
    public void closeMap(String mapName) {

    }

    @Override
    public void removeMap(String mapName) {

    }
}
