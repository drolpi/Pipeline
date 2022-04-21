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

package de.natrox.pipeline.json;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.part.storage.GlobalStorage;
import jodd.io.FileNameUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

final class JsonFileStorage implements GlobalStorage {

    private final static Logger LOGGER = LoggerFactory.getLogger(JsonFileStorage.class);

    private final JsonDocument.Factory documentFactory;
    private final Path directory;

    protected JsonFileStorage(Pipeline pipeline, Path path) {
        this.documentFactory = pipeline.documentFactory();
        this.directory = path;

        LOGGER.debug("Json storage initialized");
    }

    @Override
    public JsonDocument get(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(objectUUID, "objectUUID");

        try {
            return loadFromFile(dataClass, objectUUID);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean exists(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(objectUUID, "objectUUID");

        return Files.exists(savedFile(dataClass, objectUUID));
    }

    @Override
    public void save(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonDocument data) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(objectUUID, "objectUUID");
        Check.notNull(data, "data");

        try {
            saveJsonToFile(dataClass, objectUUID, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean remove(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(objectUUID, "objectUUID");

        if (!exists(dataClass, objectUUID))
            return false;
        try {
            Files.deleteIfExists(savedFile(dataClass, objectUUID));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public @NotNull Collection<UUID> keys(@NotNull Class<? extends PipelineData> dataClass) {
        Check.notNull(dataClass, "dataClass");

        var parentFolder = parent(dataClass);
        if (parentFolder.toFile().exists()) {
            try {
                return Files.walk(parentFolder, 1)
                    .skip(1)
                    .filter(path1 -> FileNameUtil.getExtension(path1.getFileName().toString()).equals(".json"))
                    .map(path1 -> FileNameUtil.getBaseName(path1.toString()))
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return List.of();
    }

    @Override
    public @NotNull Collection<JsonDocument> documents(@NotNull Class<? extends PipelineData> dataClass) {
        Check.notNull(dataClass, "dataClass");

        var parentFolder = parent(dataClass);
        if (parentFolder.toFile().exists()) {
            try {
                var keys = Files.walk(parentFolder, 1)
                    .skip(1)
                    .filter(path1 -> FileNameUtil.getExtension(path1.getFileName().toString()).equals(".json"))
                    .map(path1 -> FileNameUtil.getBaseName(path1.toString()))
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

                Collection<JsonDocument> documents = new ArrayList<>();
                for (UUID uuid : keys) {
                    var value = loadFromFile(dataClass, uuid);
                    documents.add(value);
                }
                return documents;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return List.of();
    }

    @Override
    public @NotNull Map<UUID, JsonDocument> entries(@NotNull Class<? extends PipelineData> dataClass) {
        Check.notNull(dataClass, "dataClass");
        return this.filter(dataClass, (key, value) -> true);
    }

    @Override
    public @NotNull Map<UUID, JsonDocument> filter(@NotNull Class<? extends PipelineData> dataClass, @NotNull BiPredicate<UUID, JsonDocument> predicate) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(predicate, "predicate");

        var parentFolder = parent(dataClass);
        if (parentFolder.toFile().exists()) {
            try {
                var keys = Files.walk(parentFolder, 1)
                    .skip(1)
                    .filter(path1 -> FileNameUtil.getExtension(path1.getFileName().toString()).equals(".json"))
                    .map(path1 -> FileNameUtil.getBaseName(path1.toString()))
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

                Map<UUID, JsonDocument> entries = new HashMap<>();
                for (UUID key : keys) {
                    var value = loadFromFile(dataClass, key);

                    if (predicate.test(key, value)) {
                        entries.put(key, value);
                    }
                }
                return entries;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Map.of();
    }

    @Override
    public void iterate(@NotNull Class<? extends PipelineData> dataClass, @NotNull BiConsumer<UUID, JsonDocument> consumer) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(consumer, "consumer");
        this.entries(dataClass).forEach(consumer);
    }

    private void saveJsonToFile(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonDocument data) throws IOException {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(objectUUID, "objectUUID");
        Check.notNull(data, "data");

        var path = savedFile(dataClass, objectUUID);

        var file = new File(path.toUri());
        if (!file.exists()) {
            if (!file.getParentFile().mkdirs() || !file.createNewFile())
                throw new RuntimeException("Could not create files for JsonFileStorage [" + path + "]");
        }
        var writer = new FileWriter(file);
        data.write(writer);
    }

    private JsonDocument loadFromFile(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) throws IOException {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(objectUUID, "objectUUID");

        var path = savedFile(dataClass, objectUUID);
        var file = new File(path.toUri());
        if (!file.exists())
            throw new RuntimeException("SavedFile does not exist for " + dataClass.getSimpleName() + ":" + objectUUID);
        return documentFactory.newDocument(path);
    }

    private Path savedFile(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Check.notNull(dataClass, "dataClass");
        Check.notNull(objectUUID, "objectUUID");

        return Paths.get(parent(dataClass).toString(), objectUUID + ".json");
    }

    private Path parent(@NotNull Class<? extends PipelineData> dataClass) {
        Check.notNull(dataClass, "dataClass");
        var storageIdentifier = AnnotationResolver.storageIdentifier(dataClass);

        return Paths.get(directory.toString(), storageIdentifier);
    }
}
