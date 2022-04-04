package de.natrox.pipeline.part;

import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.common.runnable.CatchingRunnable;
import de.natrox.pipeline.PipelineImpl;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class DataSynchronizerImpl implements DataSynchronizer {

    private final static Logger LOGGER = LogManager.logger(DataSynchronizerImpl.class);

    private final PipelineImpl pipelineImpl;
    private final ExecutorService executorService;

    public DataSynchronizerImpl(@NotNull PipelineImpl pipelineImpl) {
        this.pipelineImpl = pipelineImpl;
        this.executorService = pipelineImpl.executorService();
    }

    @Override
    public @NotNull <T extends PipelineData> CompletableFuture<Boolean> synchronize(
        @NotNull DataSourceType source,
        @NotNull DataSourceType destination,
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID objectUUID,
        @Nullable Runnable callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        Objects.requireNonNull(source, "source can't be null!");
        Objects.requireNonNull(destination, "destination can't be null!");
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        var future = new CompletableFuture<Boolean>();
        executorService.submit(new CatchingRunnable(() ->
            future.complete(doSynchronisation(source, destination, dataClass, objectUUID, callback, instanceCreator))));
        return future;
    }

    public synchronized <T extends PipelineData> boolean doSynchronisation(
        @NotNull DataSourceType source,
        @NotNull DataSourceType destination,
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID objectUUID,
        @Nullable Runnable callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        Objects.requireNonNull(source, "source can't be null!");
        Objects.requireNonNull(destination, "destination can't be null!");
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        if (source.equals(destination))
            return false;
        if (pipelineImpl.globalCache() == null && (source.equals(DataSourceType.GLOBAL_CACHE) || destination.equals(DataSourceType.GLOBAL_CACHE)))
            return false;
        if (pipelineImpl.globalStorage() == null && (source.equals(DataSourceType.GLOBAL_STORAGE) || destination.equals(DataSourceType.GLOBAL_STORAGE)))
            return false;

        var startTime = System.currentTimeMillis();
        var dataUpdater = pipelineImpl.dataUpdaterService().dataUpdater(dataClass);
        if (destination.equals(DataSourceType.LOCAL))
            dataUpdater.registerLoadingTask(objectUUID);

        if (source.equals(DataSourceType.LOCAL)) {
            if (!pipelineImpl.localCache().dataExist(dataClass, objectUUID))
                return false;
            var data = pipelineImpl.localCache().data(dataClass, objectUUID);
            if (data == null)
                return false;
            data.updateLastUse();
            data.unMarkRemoval();
            var dataToSave = data.serialize();
            LOGGER.debug("Syncing " + dataClass.getSimpleName() + " with uuid " + objectUUID + " [" + DataSourceType.LOCAL + " -> " + destination + "]"); 
            if (destination.equals(DataSourceType.GLOBAL_CACHE))
                // Local to Global Cache
                pipelineImpl.globalCache().saveData(dataClass, objectUUID, dataToSave);
            else if (destination.equals(DataSourceType.GLOBAL_STORAGE))
                // Local to Global Storage
                pipelineImpl.globalStorage().saveData(dataClass, objectUUID, dataToSave);
        } else if (source.equals(DataSourceType.GLOBAL_CACHE)) {
            if (!pipelineImpl.globalCache().dataExist(dataClass, objectUUID))
                return false;
            var globalCachedData = pipelineImpl.globalCache().loadData(dataClass, objectUUID);
            // Error while loading from redis
            if (globalCachedData == null) {
                LOGGER.debug("Trying to load from storage..."); 
                doSynchronisation(DataSourceType.GLOBAL_STORAGE, DataSourceType.LOCAL, dataClass, objectUUID, callback, instanceCreator);
                return false;
            }

            LOGGER.debug("Syncing " + dataClass.getSimpleName() + " with uuid " + objectUUID + " [" + DataSourceType.GLOBAL_CACHE + " -> " + destination + "]"); 
            if (destination.equals(DataSourceType.LOCAL)) {
                if (!pipelineImpl.localCache().dataExist(dataClass, objectUUID)) {
                    pipelineImpl.localCache().save(
                        dataClass,
                        pipelineImpl.localCache().instantiateData(pipelineImpl, dataClass, objectUUID, instanceCreator)
                    );
                }

                var data = pipelineImpl.localCache().data(dataClass, objectUUID);
                if (data == null)
                    return false;
                data.deserialize(globalCachedData);
                data.updateLastUse();
                data.loadDependentData();
                data.onLoad();
                pipelineImpl.localCache().save(dataClass, data);
            } else if (destination.equals(DataSourceType.GLOBAL_STORAGE))
                pipelineImpl.globalStorage().saveData(dataClass, objectUUID, globalCachedData);

        } else if (source.equals(DataSourceType.GLOBAL_STORAGE)) {
            if (!pipelineImpl.globalStorage().dataExist(dataClass, objectUUID))
                return false;
            var globalSavedData = pipelineImpl.globalStorage().loadData(dataClass, objectUUID);

            LOGGER.debug("Syncing " + dataClass.getSimpleName() + " with uuid " + objectUUID + " [" + DataSourceType.GLOBAL_STORAGE + " -> " + destination + "]"); 
            if (destination.equals(DataSourceType.LOCAL)) {
                if (!pipelineImpl.localCache().dataExist(dataClass, objectUUID)) {
                    pipelineImpl.localCache().save(
                        dataClass,
                        pipelineImpl.localCache().instantiateData(pipelineImpl, dataClass, objectUUID, instanceCreator)
                    );
                }

                var data = pipelineImpl.localCache().data(dataClass, objectUUID);
                if (data == null)
                    return false;
                data.deserialize(globalSavedData);
                data.updateLastUse();
                data.loadDependentData();
                data.onLoad();
                pipelineImpl.localCache().save(dataClass, data);
            } else if (destination.equals(DataSourceType.GLOBAL_CACHE))
                pipelineImpl.globalCache().saveData(dataClass, objectUUID, globalSavedData);
        }

        if (destination.equals(DataSourceType.LOCAL)) {
            var data = pipelineImpl.localCache().data(dataClass, objectUUID);
            var optional = dataUpdater.finishLoadingTask(data);
            optional.ifPresent(pipelineData -> pipelineImpl.localCache().save(dataClass, pipelineData));
        }

        if (callback != null)
            callback.run();
        LOGGER.debug("Done syncing in " + (System.currentTimeMillis() - startTime) + "ms [" + dataClass.getSimpleName() + "]"); 
        return true;
    }

    @Override
    public void load() {

    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public void shutdown() {
        try {
            LOGGER.debug("Shutting down Data Synchronizer"); 
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
            LOGGER.debug("Data Synchronizer shut down successfully"); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
