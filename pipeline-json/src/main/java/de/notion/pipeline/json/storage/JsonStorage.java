package de.notion.pipeline.json.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.filter.Filter;
import de.notion.pipeline.part.storage.GlobalStorage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class JsonStorage implements GlobalStorage {

    protected static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private final Path directory;

    public JsonStorage(String path) {
        this.directory = Paths.get(path);

        System.out.println("Json Storage initialized");
    }

    @Override
    public String loadData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        return JsonFileUtil.loadFromJson(getSaveFile(dataClass, objectUUID));
    }

    @Override
    public boolean dataExist(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        return Files.exists(getSaveFile(dataClass, objectUUID));
    }

    @Override
    public void saveData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull String dataToSave) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        Objects.requireNonNull(dataToSave, "dataToSave can't be null!");
        JsonFileUtil.saveToJson(dataToSave, getSaveFile(dataClass, objectUUID));
    }

    @Override
    public boolean removeData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        if (dataExist(dataClass, objectUUID)) {
            try {
                return Files.deleteIfExists(getSaveFile(dataClass, objectUUID));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public Set<UUID> getSavedUUIDs(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Set<UUID> foundUUIDs = new HashSet<>();
        File parentFolder = getParent(dataClass).toFile();

        if (!parentFolder.exists())
            return foundUUIDs;
        try {
            Files.walk(parentFolder.toPath(), 1).forEach(path -> {
                String fileName = path.toFile().getName().replace(".json", "");
                try {
                    UUID readUUID = UUID.fromString(fileName);
                    foundUUIDs.add(readUUID);
                } catch (IllegalArgumentException e) {
                    System.out.println("Could not read file name in JsonStorage because it is not a uuid: " + path.toFile().getAbsolutePath());
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return foundUUIDs;
    }

    @Override
    public List<UUID> filter(@NotNull Class<? extends PipelineData> type, @NotNull Filter filter) {
        List<UUID> uuids = new ArrayList<>();
        File parentFolder = getParent(type).toFile();

        System.out.println(parentFolder);

        if (!parentFolder.exists())
            return new ArrayList<>();
        for (File file : parentFolder.listFiles()) {
            System.out.println(file);
            String data = JsonFileUtil.loadFromJson(file.toPath());
            System.out.println(data);
            Map<String, Object> document = GSON.fromJson(data, Map.class);
            System.out.println(document);

            if (filter.check(document)) {
                uuids.add(UUID.fromString((String) document.get("objectUUID")));
            }
        }
        return uuids;
    }

    private Path getSaveFile(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        return Paths.get(getParent(dataClass).toString(), objectUUID + ".json");
    }

    private Path getParent(@NotNull Class<? extends PipelineData> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        String storageIdentifier = AnnotationResolver.getStorageIdentifier(dataClass);

        return Paths.get(directory.toString(), storageIdentifier);
    }
}
