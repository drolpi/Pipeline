package de.natrox.pipeline.part.local;

import com.google.common.base.Preconditions;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public final class DefaultLocalCache implements LocalCache {

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultLocalCache.class);
    private final Map<Class<? extends PipelineData>, Map<UUID, PipelineData>> dataObjects = new ConcurrentHashMap<>();

    public DefaultLocalCache() {
        LOGGER.debug("LocalCache started.");
    }

    @Override
    public synchronized <S extends PipelineData> @Nullable S get(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        if (!exists(dataClass, objectUUID))
            return null;
        S data = dataClass.cast(dataObjects.get(dataClass).get(objectUUID));
        data.updateLastUse();
        return data;
    }

    @Override
    public synchronized <S extends PipelineData> boolean exists(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        if (!dataObjects.containsKey(dataClass))
            return false;
        return dataObjects.get(dataClass).containsKey(objectUUID);
    }

    @Override
    public synchronized <S extends PipelineData> void save(@NotNull Class<? extends S> dataClass, @NotNull S data) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(data, "data");
        if (exists(dataClass, data.objectUUID()))
            return;
        if (!dataObjects.containsKey(dataClass))
            dataObjects.put(dataClass, new ConcurrentHashMap<>());
        data.updateLastUse();
        dataObjects.get(dataClass).put(data.objectUUID(), data);
    }

    @Override
    public synchronized <S extends PipelineData> boolean remove(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        if (!exists(dataClass, objectUUID))
            return false;
        dataObjects.get(dataClass).remove(objectUUID);
        if (dataObjects.get(dataClass).size() == 0)
            dataObjects.remove(dataClass);
        return true;
    }

    @Override
    public synchronized <S extends PipelineData> @NotNull Set<UUID> keys(@NotNull Class<? extends S> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        if (!dataObjects.containsKey(dataClass))
            return new HashSet<>();
        return dataObjects.get(dataClass).keySet();
    }

    @Override
    public synchronized <S extends PipelineData> @NotNull Set<S> values(@NotNull Class<? extends S> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        if (!dataObjects.containsKey(dataClass))
            return new HashSet<>();
        return dataObjects.get(dataClass).values().stream().map(dataClass::cast).collect(Collectors.toSet());
    }

    @Override
    public @NotNull <S extends PipelineData> Map<UUID, S> entries(@NotNull Class<? extends S> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        return filter(dataClass, (uuid, s) -> true);
    }

    @Override
    public @NotNull <S extends PipelineData> Map<UUID, S> filter(@NotNull Class<? extends S> dataClass, @NotNull BiPredicate<UUID, S> predicate) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(predicate, "predicate");

        if (!dataObjects.containsKey(dataClass))
            return new HashMap<>();
        Map<UUID, S> entries = new HashMap<>();
        for (var entry : dataObjects.get(dataClass).entrySet()) {
            var key = entry.getKey();
            var value = dataClass.cast(entry.getValue());

            if (predicate.test(key, value)) {
                entries.put(key, value);
            }
        }
        return entries;
    }

    @Override
    public <S extends PipelineData> void iterate(@NotNull Class<? extends S> dataClass, @NotNull BiConsumer<UUID, S> consumer) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(consumer, "consumer");
        this.entries(dataClass).forEach(consumer);
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Override
    public synchronized <S extends PipelineData> @NotNull S instantiateData(Pipeline pipeline, @NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID, @Nullable InstanceCreator<S> instanceCreator) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        if (exists(dataClass, objectUUID))
            return get(dataClass, objectUUID);

        if (instanceCreator == null)
            instanceCreator = pipeline.registry().instanceCreator(dataClass);

        S instance;
        try {
            instance = instanceCreator.get(dataClass, pipeline);
        } catch (Throwable throwable) {
            throw new RuntimeException("Error while creating instance of class " + dataClass.getSimpleName(), throwable);
        }

        var defaultObject = pipeline.documentFactory().newDocument();
        defaultObject.append("objectUUID", objectUUID.toString());

        instance.deserialize(defaultObject);
        return instance;
    }
}
