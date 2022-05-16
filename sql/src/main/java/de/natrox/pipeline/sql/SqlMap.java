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

import de.natrox.common.container.Pair;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.part.StoreMap;
import de.natrox.pipeline.stream.PipeStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class SqlMap implements StoreMap {

    //TODO: Add null checks

    private final SqlStore sqlStore;
    private final String mapName;
    private final JsonConverter jsonConverter;

    SqlMap(SqlStore sqlStore, String mapName, JsonConverter jsonConverter) {
        this.sqlStore = sqlStore;
        this.mapName = mapName;
        this.jsonConverter = jsonConverter;
    }

    @Override
    public @Nullable DocumentData get(@NotNull UUID uniqueId) {
        return sqlStore.executeQuery(
            String.format(SQLConstants.SELECT_BY_UUID, SQLConstants.TABLE_COLUMN_VAL, mapName, SQLConstants.TABLE_COLUMN_KEY),
            resultSet -> resultSet.next() ? this.jsonConverter.read(resultSet.getString(SQLConstants.TABLE_COLUMN_VAL), DocumentData.class) : null,
            null,
            uniqueId.toString()
        );
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull DocumentData document) {
        String jsonDocument = this.jsonConverter.writeAsString(document);
        if (!contains(uniqueId)) {
            sqlStore.executeUpdate(
                String.format(SQLConstants.INSERT_BY_UUID, mapName, SQLConstants.TABLE_COLUMN_KEY, SQLConstants.TABLE_COLUMN_VAL),
                uniqueId.toString(), jsonDocument
            );
        } else {
            sqlStore.executeUpdate(
                String.format(SQLConstants.UPDATE_BY_UUID, mapName, SQLConstants.TABLE_COLUMN_VAL, SQLConstants.TABLE_COLUMN_KEY),
                jsonDocument, uniqueId.toString()
            );
        }
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        return sqlStore.executeQuery(
            String.format(SQLConstants.SELECT_BY_UUID, SQLConstants.TABLE_COLUMN_KEY, mapName, SQLConstants.TABLE_COLUMN_KEY),
            ResultSet::next,
            false,
            uniqueId.toString()
        );
    }

    @Override
    public @NotNull PipeStream<UUID> keys() {
        return PipeStream.fromIterable(sqlStore.executeQuery(
            String.format(SQLConstants.SELECT_ALL, SQLConstants.TABLE_COLUMN_KEY, mapName),
            resultSet -> {
                Collection<UUID> keys = new ArrayList<>();
                while (resultSet.next()) {
                    keys.add(UUID.fromString(resultSet.getString(SQLConstants.TABLE_COLUMN_KEY)));
                }
                return keys;
            }, List.of()));
    }

    @Override
    public @NotNull PipeStream<DocumentData> values() {
        return PipeStream.fromIterable(sqlStore.executeQuery(
            String.format(SQLConstants.SELECT_ALL, SQLConstants.TABLE_COLUMN_VAL, mapName),
            resultSet -> {
                Collection<DocumentData> documents = new ArrayList<>();
                while (resultSet.next()) {
                    documents.add(jsonConverter.read(resultSet.getString(SQLConstants.TABLE_COLUMN_VAL), DocumentData.class));
                }
                return documents;
            }, List.of()));
    }

    @Override
    public @NotNull PipeStream<Pair<UUID, DocumentData>> entries() {
        return PipeStream.fromMap(sqlStore.executeQuery(
            String.format(SQLConstants.SELECT_ALL, mapName),
            resultSet -> {
                Map<UUID, DocumentData> map = new HashMap<>();
                while (resultSet.next()) {
                    map.put(
                        UUID.fromString(resultSet.getString(SQLConstants.TABLE_COLUMN_KEY)),
                        jsonConverter.read(resultSet.getString(SQLConstants.TABLE_COLUMN_VAL), DocumentData.class)
                    );
                }
                return map;
            }, Map.of()));
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        sqlStore.executeUpdate(
            String.format(SQLConstants.DELETE_BY_UUID, mapName, SQLConstants.TABLE_COLUMN_KEY),
            uniqueId.toString()
        );
    }

    @Override
    public void clear() {

    }

    @Override
    public long size() {
        return 0;
    }
}
