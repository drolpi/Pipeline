package de.notion.pipeline.part;

import com.google.gson.JsonObject;
import de.notion.common.runnable.CatchingRunnable;
import de.notion.pipeline.PipelineManager;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.local.updater.LoadingTaskManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class PipelineDataSynchronizerImpl implements PipelineDataSynchronizer {

    private final PipelineManager pipelineManager;
    private final ExecutorService executorService;

    public PipelineDataSynchronizerImpl(PipelineManager pipelineManager) {
        this.pipelineManager = pipelineManager;
        this.executorService = pipelineManager.executorService();
    }

    @NotNull
    @Override
    public CompletableFuture<Boolean> synchronize(@NotNull DataSourceType source, @NotNull DataSourceType destination, @NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        return synchronize(source, destination, dataClass, objectUUID, null);
    }

    @NotNull
    @Override
    public synchronized CompletableFuture<Boolean> synchronize(@NotNull DataSourceType source, @NotNull DataSourceType destination, @NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @Nullable Runnable callback) {
        Objects.requireNonNull(source, "source can't be null!");
        Objects.requireNonNull(destination, "destination can't be null!");
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        var future = new CompletableFuture<Boolean>();
        executorService.submit(new CatchingRunnable(() -> future.complete(doSynchronisation(source, destination, dataClass, objectUUID, callback))));
        return future;
    }

    public synchronized boolean doSynchronisation(@NotNull DataSourceType source, @NotNull DataSourceType destination, @NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @Nullable Runnable callback) {
        Objects.requireNonNull(source, "source can't be null!");
        Objects.requireNonNull(destination, "destination can't be null!");
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        if (source.equals(destination))
            return false;
        if (pipelineManager.globalCache() == null && (source.equals(DataSourceType.GLOBAL_CACHE) || destination.equals(DataSourceType.GLOBAL_CACHE)))
            return false;
        if (pipelineManager.globalStorage() == null && (source.equals(DataSourceType.GLOBAL_STORAGE) || destination.equals(DataSourceType.GLOBAL_STORAGE)))
            return false;

        var startTime = System.currentTimeMillis();
        LoadingTaskManager loadingTaskManager = pipelineManager.dataUpdaterService().dataUpdater(dataClass).loadingTaskManager();
        if (destination.equals(DataSourceType.LOCAL))
            loadingTaskManager.registerLoadingTask(objectUUID);

        System.out.println("Syncing " + dataClass.getSimpleName() + " with uuid " + objectUUID + " [" + source + " -> " + destination + "]"); //DEBUG

        if (source.equals(DataSourceType.LOCAL)) {
            if (!pipelineManager.localCache().dataExist(dataClass, objectUUID))
                return false;
            var data = pipelineManager.localCache().data(dataClass, objectUUID);
            if (data == null)
                return false;
            data.updateLastUse();
            data.unMarkRemoval();
            JsonObject dataToSave = data.serialize();
            // Local to Global Cache
            if (destination.equals(DataSourceType.GLOBAL_CACHE))
                pipelineManager.globalCache().saveData(dataClass, objectUUID, dataToSave);
                // Local to Global Storage
            else if (destination.equals(DataSourceType.GLOBAL_STORAGE))
                pipelineManager.globalStorage().saveData(dataClass, objectUUID, dataToSave);
        } else if (source.equals(DataSourceType.GLOBAL_CACHE)) {
            if (!pipelineManager.globalCache().dataExist(dataClass, objectUUID))
                return false;
            JsonObject globalCachedData = pipelineManager.globalCache().loadData(dataClass, objectUUID);
            // Error while loading from redis
            if (globalCachedData == null) {
                System.out.println("Trying to load from storage...");
                doSynchronisation(DataSourceType.GLOBAL_STORAGE, DataSourceType.LOCAL, dataClass, objectUUID, callback);
                return false;
            }

            if (destination.equals(DataSourceType.LOCAL)) {
                if (!pipelineManager.localCache().dataExist(dataClass, objectUUID)) {
                    pipelineManager.localCache().save(dataClass, pipelineManager.localCache().instantiateData(pipelineManager, dataClass, objectUUID, null));
                }

                var data = pipelineManager.localCache().data(dataClass, objectUUID);
                if (data == null)
                    return false;
                data.deserialize(globalCachedData);
                data.updateLastUse();
                data.loadDependentData();
                data.onLoad();
                pipelineManager.localCache().save(dataClass, data);
            } else if (destination.equals(DataSourceType.GLOBAL_STORAGE))
                pipelineManager.globalStorage().saveData(dataClass, objectUUID, globalCachedData);

        } else if (source.equals(DataSourceType.GLOBAL_STORAGE)) {
            if (!pipelineManager.globalStorage().dataExist(dataClass, objectUUID))
                return false;
            var globalSavedData = pipelineManager.globalStorage().loadData(dataClass, objectUUID);

            if (destination.equals(DataSourceType.LOCAL)) {
                if (!pipelineManager.localCache().dataExist(dataClass, objectUUID)) {
                    pipelineManager.localCache().save(dataClass, pipelineManager.localCache().instantiateData(pipelineManager, dataClass, objectUUID, null));
                }

                var data = pipelineManager.localCache().data(dataClass, objectUUID);
                if (data == null)
                    return false;
                data.deserialize(globalSavedData);
                data.updateLastUse();
                data.loadDependentData();
                data.onLoad();
                pipelineManager.localCache().save(dataClass, data);
            } else if (destination.equals(DataSourceType.GLOBAL_CACHE))
                pipelineManager.globalCache().saveData(dataClass, objectUUID, globalSavedData);
        }

        if (destination.equals(DataSourceType.LOCAL)) {
            var data = pipelineManager.localCache().data(dataClass, objectUUID);
            var optional = loadingTaskManager.finishLoadingTask(data);
            if (optional.isPresent())
                pipelineManager.localCache().save(dataClass, optional.get());
        }

        System.out.println("Done syncing in " + (System.currentTimeMillis() - startTime) + "ms [" + dataClass.getSimpleName() + "]"); //DEBUG
        if (callback != null)
            callback.run();
        return true;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public void shutdown() {
        try {
            System.out.println("Shutting down Data Synchronizer");
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Data Synchronizer shut down successfully");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
