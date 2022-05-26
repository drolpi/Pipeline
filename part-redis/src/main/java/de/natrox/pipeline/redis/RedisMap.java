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

package de.natrox.pipeline.redis;

import de.natrox.common.container.Pair;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.mapper.DocumentMapper;
import de.natrox.pipeline.part.StoreMap;
import de.natrox.pipeline.stream.PipeStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RBinaryStream;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

final class RedisMap implements StoreMap {

    private final RedisStore redisStore;
    private final RedissonClient redissonClient;
    private final String mapName;
    private final DocumentMapper documentMapper;

    RedisMap(RedisStore redisStore, String mapName, DocumentMapper documentMapper) {
        this.redisStore = redisStore;
        this.redissonClient = redisStore.redissonClient();
        this.mapName = mapName;
        this.documentMapper = documentMapper;
    }

    @Override
    public @Nullable DocumentData get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        RBinaryStream stream = this.stream(uniqueId);
        if (!stream.isExists())
            return null;

        byte[] bytes = stream.get();
        if (bytes == null)
            return null;

        return this.documentMapper.read(bytes);
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull DocumentData documentData) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(documentData, "documentData");

        byte[] bytes = this.documentMapper.write(documentData);
        RBinaryStream stream = this.stream(uniqueId);
        stream.set(bytes);
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        RBinaryStream stream = this.stream(uniqueId);
        return stream.isExists();
    }

    @Override
    public @NotNull PipeStream<UUID> keys() {
        List<UUID> keys = this.redisStore.keys(this.mapName)
            .stream()
            .map(s -> UUID.fromString(s.split(":")[2]))
            .collect(Collectors.toList());
        return PipeStream.fromIterable(keys);
    }

    @Override
    public @NotNull PipeStream<DocumentData> values() {
        Set<String> keys = this.redisStore.keys(this.mapName);

        List<DocumentData> documents = new ArrayList<>();
        for (var key : keys) {
            RBinaryStream stream = this.redissonClient.getBinaryStream(key);

            if (!stream.isExists())
                continue;

            byte[] bytes = stream.get();
            if (bytes == null)
                continue;

            documents.add(this.documentMapper.read(bytes));
        }
        return PipeStream.fromIterable(documents);
    }

    @Override
    public @NotNull PipeStream<Pair<UUID, DocumentData>> entries() {
        PipeStream<UUID> keys = this.keys();

        Map<UUID, DocumentData> entries = new HashMap<>();
        for (var key : keys) {
            RBinaryStream stream = this.stream(key);

            if (!stream.isExists())
                continue;

            byte[] bytes = stream.get();
            if (bytes == null)
                continue;

            entries.put(key, this.documentMapper.read(bytes));
        }
        return PipeStream.fromMap(entries);
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        RBinaryStream stream = this.stream(uniqueId);
        stream.delete();
    }

    @Override
    public void clear() {
        Set<String> keys = this.redisStore.keys(this.mapName);
        RKeys redisKeys = this.redissonClient.getKeys();
        redisKeys.delete(keys.toArray(new String[0]));
    }

    @Override
    public long size() {
        return this.redisStore.keys(this.mapName).size();
    }

    private RBinaryStream stream(UUID uniqueId) {
        return this.redissonClient.getBinaryStream("Cache:" + this.mapName + ":" + uniqueId.toString());
    }
}
