package de.natrox.pipeline.json;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.part.storage.GlobalStorage;
import jodd.io.FileNameUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class JsonStorage implements GlobalStorage {

    private final static Logger LOGGER = LoggerFactory.getLogger(JsonStorage.class);

    private final Gson gson;
    private final Path directory;

    protected JsonStorage(Pipeline pipeline, Path path) {
        this.gson = pipeline.gson();
        this.directory = path;

        LOGGER.debug("Json storage initialized");
    }

    @Override
    public JsonObject loadData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        try {
            return loadFromFile(dataClass, objectUUID);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean dataExist(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        return Files.exists(savedFile(dataClass, objectUUID));
    }

    @Override
    public void saveData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonObject data) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        Preconditions.checkNotNull(data, "data");

        try {
            saveJsonToFile(dataClass, objectUUID, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean removeData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        if (!dataExist(dataClass, objectUUID))
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
    public @NotNull Collection<UUID> savedUUIDs(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        return data(dataClass).keySet();
    }

    @Override
    public @NotNull Map<UUID, JsonObject> data(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        var parentFolder = parent(dataClass);
        if (!parentFolder.toFile().exists())
            return Map.of();
        try {
            var data = new HashMap<UUID, JsonObject>();

            Files.walk(parentFolder, 1)
                .skip(1)
                .filter(path1 -> FileNameUtil.getExtension(path1.getFileName().toString()).equals(".json"))
                .map(path1 -> FileNameUtil.getBaseName(path1.toString()))
                .map(UUID::fromString)
                .forEach(uuid -> {
                    var jsonObject = loadData(dataClass, uuid);
                    if (jsonObject == null)
                        return;

                    data.put(uuid, jsonObject);
                });

            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Map.of();
    }

    private void saveJsonToFile(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonObject data) throws IOException {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        Preconditions.checkNotNull(data, "data");

        if (data.isJsonNull())
            return;
        var path = savedFile(dataClass, objectUUID);

        var file = new File(path.toUri());
        if (!file.exists()) {
            if (!file.getParentFile().mkdirs() || !file.createNewFile())
                throw new RuntimeException("Could not create files for JsonFileStorage [" + path + "]");
        }
        try (var writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        }
    }

    private JsonObject loadFromFile(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) throws IOException {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        var path = savedFile(dataClass, objectUUID);
        var file = new File(path.toUri());
        if (!file.exists())
            throw new RuntimeException("SavedFile does not exist for " + dataClass.getSimpleName() + ":" + objectUUID);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path.toFile()))) {
            return JsonParser.parseReader(bufferedReader).getAsJsonObject();
        }
    }

    private Path savedFile(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        return Paths.get(parent(dataClass).toString(), objectUUID + ".json");
    }

    private Path parent(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        var storageIdentifier = AnnotationResolver.storageIdentifier(dataClass);

        return Paths.get(directory.toString(), storageIdentifier);
    }
}
