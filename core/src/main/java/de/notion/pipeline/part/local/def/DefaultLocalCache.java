package de.notion.pipeline.part.local.def;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.local.LocalCache;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

    @Override
    public synchronized <S extends PipelineData> S instantiateData(Pipeline pipeline, @NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID) {
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");

        if (dataExist(dataClass, objectUUID))
            return data(dataClass, objectUUID);

        //TODO:
        /*
        Injector injector = pipeline.getInjectorProvider().getInjector(dataClass, pipeline);
        S instance = injector.getInstance(dataClass);
         */

        S instance = null;
        try {
            Constructor<? extends S> constructor = dataClass.getConstructor(Pipeline.class);
            constructor.setAccessible(true);

            instance = constructor.newInstance(pipeline);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }


        String defaultData = ("{\"objectUUID\": \"" + objectUUID + "\"}");
        instance.deserialize(defaultData);

        return instance;
    }
}
