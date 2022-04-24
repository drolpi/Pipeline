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

package de.natrox.pipeline.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import de.natrox.common.container.Pair;
import de.natrox.pipeline.document.Document;
import de.natrox.pipeline.part.map.PartMap;
import de.natrox.pipeline.stream.PipelineStream;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

final class MongoMap implements PartMap {

    private static final String KEY_NAME = "Key";
    private static final String VALUE_NAME = "Value";
    private static final IndexOptions UNIQUE_KEY_OPTIONS = new IndexOptions().unique(true);
    private static final UpdateOptions INSERT_OR_REPLACE_OPTIONS = new UpdateOptions().upsert(true);

    private final MongoCollection<org.bson.Document> collection;
    public MongoMap(MongoCollection<org.bson.Document> collection) {
        this.collection = collection;
    }

    @Override
    public Document get(@NotNull UUID uniqueId) {
        return null;
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull Document document) {

    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        return false;
    }

    @Override
    public @NotNull PipelineStream<UUID> keys() {
        return null;
    }

    @Override
    public @NotNull PipelineStream<Document> values() {
        return null;
    }

    @Override
    public @NotNull PipelineStream<Pair<UUID, Document>> entries() {
        return null;
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {

    }

    @Override
    public void clear() {

    }

    @Override
    public long size() {
        return 0;
    }
}
