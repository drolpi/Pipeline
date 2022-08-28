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

import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.store.StoreMap;
import de.natrox.pipeline.repository.QueryStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.util.*;

@SuppressWarnings("ClassCanBeRecord")
final class SqlMap implements StoreMap {

    private final SqlStore sqlStore;
    private final String mapName;

    SqlMap(SqlStore sqlStore, String mapName) {
        this.sqlStore = sqlStore;
        this.mapName = mapName;
        this.sqlStore.executeUpdate("CREATE TABLE IF NOT EXISTS `" + mapName + "` (`key` TEXT, `data` LONGBLOB);");
    }

    @Override
    public @Nullable Object get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        return this.sqlStore.executeQuery(
            "SELECT `data` FROM `" + this.mapName + "` WHERE `key` = ?",
            resultSet -> resultSet.next() ? resultSet.getObject("data") : null,
            uniqueId.toString()
        );
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull Object data, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(data, "data");

        if (!this.contains(uniqueId)) {
            this.sqlStore.executeUpdate("INSERT INTO " + this.mapName + " (`key`, `data`) VALUES (?, ?)", statement -> {
                statement.setString(1, uniqueId.toString());
                statement.setObject(2, data);
            });
        } else {
            this.sqlStore.executeUpdate("UPDATE " + this.mapName + " SET `data` = ? WHERE `key` = ?", statement -> {
                statement.setString(1, uniqueId.toString());
                statement.setObject(2, data);
            });
        }
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        return this.sqlStore.executeQuery(
            "SELECT `key` FROM `" + this.mapName + "` WHERE `key` = ?",
            ResultSet::next,
            uniqueId.toString()
        );
    }

    @Override
    public @NotNull Collection<UUID> keys() {
        return this.sqlKeys().stream().map(UUID::fromString).toList();
    }

    @Override
    public @NotNull Collection<Object> values() {
        return this.sqlStore.executeQuery(
            "SELECT `data` FROM " + this.mapName,
            resultSet -> {
                List<Object> documents = new ArrayList<>();
                while (resultSet.next())
                    documents.add(resultSet.getObject("data"));

                return documents;
            });
    }

    @Override
    public @NotNull Map<UUID, Object> entries() {
        return this.sqlStore.executeQuery(
            "SELECT * FROM " + this.mapName,
            resultSet -> {
                Map<UUID, Object> map = new HashMap<>();
                while (resultSet.next())
                    map.put(
                        UUID.fromString(resultSet.getString("key")),
                        resultSet.getObject("data")
                    );
                return map;
            });
    }

    @Override
    public void remove(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        this.sqlStore.executeUpdate(
            "DELETE FROM `" + this.mapName + "` WHERE `key` = ?",
            uniqueId.toString()
        );
    }

    @Override
    public void clear() {
        this.sqlStore.executeUpdate("DELETE FROM Test");
    }

    @Override
    public long size() {
        return this.keys().size();
    }

    private List<String> sqlKeys() {
        return this.sqlStore.executeQuery(
            "SELECT `key` FROM " + this.mapName,
            resultSet -> {
                List<String> keys = new ArrayList<>();
                while (resultSet.next())
                    keys.add(resultSet.getString("key"));

                return keys;
            });
    }
}
