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

package de.natrox.pipeline.json;

import de.natrox.common.container.Pair;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.mapper.DocumentMapper;
import de.natrox.pipeline.part.StoreMap;
import de.natrox.pipeline.stream.PipeStream;
import jodd.io.FileNameUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

final class JsonMap implements StoreMap {

    private final Path mapPath;
    private final DocumentMapper documentMapper;

    public JsonMap(String mapName, Path directory, DocumentMapper documentMapper) {
        this.mapPath = directory.resolve(mapName);
        this.documentMapper = documentMapper;
    }

    @Override
    public @Nullable DocumentData get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        try {
            return this.loadFromFile(uniqueId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull DocumentData documentData) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(documentData, "documentData");

        try {
            this.saveJsonToFile(uniqueId, documentData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        return Files.exists(this.savedFile(uniqueId));
    }

    @Override
    public @NotNull PipeStream<UUID> keys() {
        if (Files.notExists(this.mapPath))
            return PipeStream.empty();

        try (Stream<Path> stream = Files.walk(this.mapPath, 1)) {
            return PipeStream.fromIterable(
                stream
                    .skip(1)
                    .filter(path -> FileNameUtil.getExtension(path.getFileName().toString()).equals(".json"))
                    .map(path -> FileNameUtil.getBaseName(path.toString()))
                    .map(UUID::fromString)
                    .toList()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return PipeStream.empty();
    }

    @Override
    public @NotNull PipeStream<DocumentData> values() {
        if (Files.notExists(this.mapPath))
            return PipeStream.empty();

        try {
            List<DocumentData> documents = new ArrayList<>();
            for (UUID key : this.keys()) {
                documents.add(this.loadFromFile(key));
            }

            return PipeStream.fromIterable(documents);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return PipeStream.empty();
    }

    @Override
    public @NotNull PipeStream<Pair<UUID, DocumentData>> entries() {
        if (Files.notExists(this.mapPath))
            return PipeStream.empty();

        try {
            Map<UUID, DocumentData> entries = new HashMap<>();
            for (UUID key : this.keys()) {
                entries.put(key, this.loadFromFile(key));
            }

            return PipeStream.fromMap(entries);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return PipeStream.empty();
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
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
            this.remove(key);
        }
    }

    @Override
    public long size() {
        return this.keys().size();
    }

    private DocumentData loadFromFile(@NotNull UUID uniqueId) throws IOException {
        Check.notNull(uniqueId, "uniqueId");

        Path path = this.savedFile(uniqueId);
        File file = new File(path.toUri());
        if (!file.exists())
            return null;
        return this.documentMapper.read(path);
    }

    private void saveJsonToFile(@NotNull UUID objectUUID, @NotNull DocumentData documentData) throws IOException {
        Check.notNull(objectUUID, "objectUUID");
        Check.notNull(documentData, "documentData");

        Path path = this.savedFile(objectUUID);
        File file = new File(path.toUri());
        if (!file.exists()) {
            if (!file.getParentFile().mkdirs() || !file.createNewFile())
                throw new RuntimeException("Could not create files for JsonMap in " + path);
        }

        FileWriter writer = new FileWriter(file);
        this.documentMapper.write(writer, documentData);
    }

    private Path savedFile(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        return this.mapPath.resolve(Path.of(uniqueId + ".json"));
    }

}
