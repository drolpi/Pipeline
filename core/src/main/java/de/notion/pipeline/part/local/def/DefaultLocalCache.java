package de.notion.pipeline.part.local.def;

import com.google.gson.JsonObject;
import de.notion.pipeline.Pipeline;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.datatype.instance.InstanceCreator;
import de.notion.pipeline.part.local.LocalCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultLocalCache implements LocalCache {

    private final Map<Class<? extends PipelineData>, Map<UUID, PipelineData>> dataObjects = new ConcurrentHashMap<>();

    public DefaultLocalCache() {
        System.out.println("LocalCache started");
    }

    @Nullable
    @Override
    public synchronized <S extends PipelineData> S data(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        if (!dataExist(dataClass, objectUUID))
            return null;
        S data = dataClass.cast(dataObjects.get(dataClass).get(objectUUID));
        data.updateLastUse();
        return data;
    }

    @NotNull
    @Override
    public synchronized <S extends PipelineData> Set<S> allData(@NotNull Class<? extends S> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        return savedUUIDs(dataClass).stream().map(uuid -> data(dataClass, uuid)).collect(Collectors.toSet());
    }

    @Override
    public synchronized <S extends PipelineData> void save(@NotNull Class<? extends S> dataClass, @NotNull S data) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(data, "data can't be null!");
        if (dataExist(dataClass, data.objectUUID()))
            return;
        if (!dataObjects.containsKey(dataClass))
            dataObjects.put(dataClass, new ConcurrentHashMap<>());
        data.updateLastUse();
        dataObjects.get(dataClass).put(data.objectUUID(), data);
    }

    @Override
    public synchronized <S extends PipelineData> boolean dataExist(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        if (!dataObjects.containsKey(dataClass))
            return false;
        return dataObjects.get(dataClass).containsKey(objectUUID);
    }

    @Override
    public synchronized <S extends PipelineData> boolean remove(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        if (!dataExist(dataClass, objectUUID))
            return false;
        dataObjects.get(dataClass).remove(objectUUID);
        if (dataObjects.get(dataClass).size() == 0)
            dataObjects.remove(dataClass);
        return true;
    }

    @Override
    public synchronized <S extends PipelineData> Set<UUID> savedUUIDs(@NotNull Class<? extends S> dataClass) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        if (!dataObjects.containsKey(dataClass))
            return new HashSet<>();
        return dataObjects.get(dataClass).keySet();
    }

    @NotNull
    @Override
    public synchronized <S extends PipelineData> S instantiateData(Pipeline pipeline, @NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID, @Nullable InstanceCreator<S> instanceCreator) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");

        if (dataExist(dataClass, objectUUID))
            return data(dataClass, objectUUID);

        if (instanceCreator == null)
            instanceCreator = pipeline.registry().instanceCreator(dataClass);

        S instance = null;
        try {
            instance = instanceCreator.get(dataClass, pipeline);
        } catch (Throwable throwable) {
            throw new RuntimeException("Error while creating instance of class " + dataClass.getSimpleName(), throwable);
        }

        var defaultObject = new JsonObject();
        defaultObject.addProperty("objectUUID", objectUUID.toString());

        instance.deserialize(defaultObject);
        return instance;
    }
}
