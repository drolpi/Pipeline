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
import de.natrox.common.validate.Check;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.part.StoreMap;
import de.natrox.pipeline.stream.PipeStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class SqlMap implements StoreMap {

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
        Check.notNull(uniqueId, "uniqueId");
        return this.sqlStore.executeQuery(
            String.format(SQLConstants.SELECT_BY_UUID, SQLConstants.COLUMN_VAL, this.mapName, SQLConstants.COLUMN_KEY),
            resultSet -> resultSet.next() ? this.jsonConverter.read(resultSet.getString(SQLConstants.COLUMN_VAL), DocumentData.class) : null,
            null,
            uniqueId.toString()
        );
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull DocumentData documentData) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(documentData, "documentData");

        String jsonDocument = this.jsonConverter.writeAsString(documentData);
        if (!this.contains(uniqueId)) {
            this.sqlStore.executeUpdate(
                String.format(SQLConstants.INSERT_BY_UUID, this.mapName, SQLConstants.COLUMN_KEY, SQLConstants.COLUMN_VAL),
                uniqueId.toString(), jsonDocument
            );
        } else {
            this.sqlStore.executeUpdate(
                String.format(SQLConstants.UPDATE_BY_UUID, this.mapName, SQLConstants.COLUMN_VAL, SQLConstants.COLUMN_KEY),
                jsonDocument, uniqueId.toString()
            );
        }
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        return this.sqlStore.executeQuery(
            String.format(SQLConstants.SELECT_BY_UUID, SQLConstants.COLUMN_KEY, this.mapName, SQLConstants.COLUMN_KEY),
            ResultSet::next,
            false,
            uniqueId.toString()
        );
    }

    @Override
    public @NotNull PipeStream<UUID> keys() {
        return this.sqlStore.executeQuery(
            String.format(SQLConstants.SELECT_ALL, SQLConstants.COLUMN_KEY, this.mapName),
            resultSet -> {
                List<UUID> keys = new ArrayList<>();
                while (resultSet.next())
                    keys.add(UUID.fromString(resultSet.getString(SQLConstants.COLUMN_KEY)));

                return PipeStream.fromIterable(keys);
            }, PipeStream.empty());
    }

    @Override
    public @NotNull PipeStream<DocumentData> values() {
        return this.sqlStore.executeQuery(
            String.format(SQLConstants.SELECT_ALL, SQLConstants.COLUMN_VAL, this.mapName),
            resultSet -> {
                List<DocumentData> documents = new ArrayList<>();
                while (resultSet.next())
                    documents.add(this.jsonConverter.read(resultSet.getString(SQLConstants.COLUMN_VAL), DocumentData.class));

                return PipeStream.fromIterable(documents);
            }, PipeStream.empty());
    }

    @Override
    public @NotNull PipeStream<Pair<UUID, DocumentData>> entries() {
        return this.sqlStore.executeQuery(
            String.format(SQLConstants.SELECT_ALL, SQLConstants.EVERY, this.mapName),
            resultSet -> {
                Map<UUID, DocumentData> map = new HashMap<>();
                while (resultSet.next())
                    map.put(
                        UUID.fromString(resultSet.getString(SQLConstants.COLUMN_KEY)),
                        this.jsonConverter.read(resultSet.getString(SQLConstants.COLUMN_VAL), DocumentData.class)
                    );
                return PipeStream.fromMap(map);
            }, PipeStream.empty());
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        this.sqlStore.executeUpdate(
            String.format(SQLConstants.DELETE_BY_UUID, this.mapName, SQLConstants.COLUMN_KEY),
            uniqueId.toString()
        );
    }

    @Override
    public void clear() {
        //TODO:
    }

    @Override
    public long size() {
        //TODO:
        return 0;
    }
}
