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

package de.natrox.pipeline.bin;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.store.StoreMap;
import de.natrox.pipeline.repository.QueryStrategy;
import jodd.io.FileNameUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

final class BinMap implements StoreMap {

    private final Path mapPath;

    public BinMap(String mapName, Path directory) {
        this.mapPath = directory.resolve(mapName);
    }

    @Override
    public byte @Nullable [] get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        try {
            return this.loadFromFile(uniqueId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void put(@NotNull UUID uniqueId, byte @NotNull [] data, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(data, "data");

        try {
            this.saveToFile(uniqueId, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        return Files.exists(this.savedFile(uniqueId));
    }

    @Override
    public @NotNull Collection<UUID> keys() {
        if (Files.notExists(this.mapPath))
            return Set.of();

        try (Stream<Path> stream = Files.walk(this.mapPath, 1)) {
            return stream
                .skip(1)
                .filter(path -> FileNameUtil.getExtension(path.getFileName().toString()).equals(".bin"))
                .map(path -> FileNameUtil.getBaseName(path.toString()))
                .map(UUID::fromString)
                .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Set.of();
    }

    @Override
    public @NotNull Collection<byte[]> values() {
        if (Files.notExists(this.mapPath))
            return Set.of();

        try {
            List<byte[]> documents = new ArrayList<>();
            for (UUID key : this.keys()) {
                documents.add(this.loadFromFile(key));
            }

            return documents;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Set.of();
    }

    @Override
    public @NotNull Map<UUID, byte[]> entries() {
        if (Files.notExists(this.mapPath))
            return Map.of();

        try {
            Map<UUID, byte[]> entries = new HashMap<>();
            for (UUID key : this.keys()) {
                entries.put(key, this.loadFromFile(key));
            }

            return entries;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Map.of();
    }

    @Override
    public void remove(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        Path path = this.savedFile(uniqueId);

        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {
        for (UUID key : this.keys()) {
            this.remove(key, QueryStrategy.LOCAL_CACHE);
        }
    }

    @Override
    public long size() {
        return this.keys().size();
    }

    private byte[] loadFromFile(@NotNull UUID uniqueId) throws IOException {
        Check.notNull(uniqueId, "uniqueId");

        Path path = this.savedFile(uniqueId);
        if (Files.notExists(path))
            return null;
        return Files.readAllBytes(path);
    }

    private void saveToFile(@NotNull UUID objectUUID, byte @NotNull [] data) throws IOException {
        Check.notNull(objectUUID, "objectUUID");
        Check.notNull(data, "data");

        Path path = this.savedFile(objectUUID);
        if (Files.notExists(path)) {
            Path parent = path.getParent();
            if (Files.notExists(parent)) {
                Files.createDirectories(parent);
            }

            Files.createFile(path);
        }

        Files.write(path, data);
    }

    private Path savedFile(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        return this.mapPath.resolve(Path.of(uniqueId + ".bin"));
    }

}
