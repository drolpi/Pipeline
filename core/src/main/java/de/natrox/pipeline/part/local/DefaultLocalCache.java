package de.natrox.pipeline.part.local;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class DefaultLocalCache implements LocalCache {

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultLocalCache.class);
    private final Map<Class<? extends PipelineData>, Map<UUID, PipelineData>> dataObjects = new ConcurrentHashMap<>();

    public DefaultLocalCache() {
        LOGGER.debug("LocalCache started.");
    }

    @Override
    public synchronized <S extends PipelineData> @Nullable S data(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        if (!dataExist(dataClass, objectUUID))
            return null;
        S data = dataClass.cast(dataObjects.get(dataClass).get(objectUUID));
        data.updateLastUse();
        return data;
    }

    @Override
    public synchronized <S extends PipelineData> @NotNull Set<S> allData(@NotNull Class<? extends S> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        return savedUUIDs(dataClass).stream().map(uuid -> data(dataClass, uuid)).collect(Collectors.toSet());
    }

    @Override
    public synchronized <S extends PipelineData> void save(@NotNull Class<? extends S> dataClass, @NotNull S data) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(data, "data");
        if (dataExist(dataClass, data.objectUUID()))
            return;
        if (!dataObjects.containsKey(dataClass))
            dataObjects.put(dataClass, new ConcurrentHashMap<>());
        data.updateLastUse();
        dataObjects.get(dataClass).put(data.objectUUID(), data);
    }

    @Override
    public synchronized <S extends PipelineData> boolean dataExist(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        if (!dataObjects.containsKey(dataClass))
            return false;
        return dataObjects.get(dataClass).containsKey(objectUUID);
    }

    @Override
    public synchronized <S extends PipelineData> boolean remove(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        if (!dataExist(dataClass, objectUUID))
            return false;
        dataObjects.get(dataClass).remove(objectUUID);
        if (dataObjects.get(dataClass).size() == 0)
            dataObjects.remove(dataClass);
        return true;
    }

    @Override
    public synchronized <S extends PipelineData> @NotNull Set<UUID> savedUUIDs(@NotNull Class<? extends S> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        if (!dataObjects.containsKey(dataClass))
            return new HashSet<>();
        return dataObjects.get(dataClass).keySet();
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Override
    public synchronized <S extends PipelineData> @NotNull S instantiateData(Pipeline pipeline, @NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID, @Nullable InstanceCreator<S> instanceCreator) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        if (dataExist(dataClass, objectUUID))
            return data(dataClass, objectUUID);

        if (instanceCreator == null)
            instanceCreator = pipeline.registry().instanceCreator(dataClass);

        S instance;
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
