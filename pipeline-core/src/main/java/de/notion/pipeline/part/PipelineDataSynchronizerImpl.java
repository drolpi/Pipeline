package de.notion.pipeline.part;

import de.notion.common.runnable.CatchingRunnable;
import de.notion.pipeline.PipelineManager;
import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PipelineDataSynchronizerImpl implements PipelineDataSynchronizer {

    private final PipelineManager pipelineManager;

    public PipelineDataSynchronizerImpl(PipelineManager pipelineManager) {
        this.pipelineManager = pipelineManager;
    }

    @Override
    public CompletableFuture<Boolean> synchronize(@NotNull DataSourceType source, @NotNull DataSourceType destination, @NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        return synchronize(source, destination, dataClass, objectUUID, null);
    }

    @Override
    public synchronized CompletableFuture<Boolean> synchronize(@NotNull DataSourceType source, @NotNull DataSourceType destination, @NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @Nullable Runnable callback) {
        Objects.requireNonNull(source, "source can't be null!");
        Objects.requireNonNull(destination, "destination can't be null!");
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        pipelineManager.getExecutorService().submit(new CatchingRunnable(() -> {
            future.complete(doSynchronisation(source, destination, dataClass, objectUUID, callback));
        }));
        return future;
    }

    public synchronized boolean doSynchronisation(@NotNull DataSourceType source, @NotNull DataSourceType destination, @NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @Nullable Runnable callback) {
        Objects.requireNonNull(source, "source can't be null!");
        Objects.requireNonNull(destination, "destination can't be null!");
        Objects.requireNonNull(dataClass, "dataClass can't be null!");
        Objects.requireNonNull(objectUUID, "objectUUID can't be null!");
        if (source.equals(destination))
            return false;
        if (pipelineManager.getGlobalCache() == null && (source.equals(DataSourceType.GLOBAL_CACHE) || destination.equals(DataSourceType.GLOBAL_CACHE)))
            return false;
        if (pipelineManager.getGlobalStorage() == null && (source.equals(DataSourceType.GLOBAL_STORAGE) || destination.equals(DataSourceType.GLOBAL_STORAGE)))
            return false;

        long startTime = System.currentTimeMillis();

        System.out.println("Syncing " + dataClass.getSimpleName() + " with uuid " + objectUUID + " [" + source + " -> " + destination + "]"); //DEBUG

        if (source.equals(DataSourceType.LOCAL)) {
            if (!pipelineManager.getLocalCache().dataExist(dataClass, objectUUID))
                return false;
            PipelineData data = pipelineManager.getLocalCache().getData(dataClass, objectUUID);
            if (data == null)
                return false;
            data.updateLastUse();
            data.unMarkRemoval();
            String dataToSave = data.serialize();
            // Local to Global Cache
            if (destination.equals(DataSourceType.GLOBAL_CACHE))
                pipelineManager.getGlobalCache().saveData(dataClass, objectUUID, dataToSave);
                // Local to Global Storage
            else if (destination.equals(DataSourceType.GLOBAL_STORAGE))
                pipelineManager.getGlobalStorage().saveData(dataClass, objectUUID, dataToSave);
        } else if (source.equals(DataSourceType.GLOBAL_CACHE)) {
            if (!pipelineManager.getGlobalCache().dataExist(dataClass, objectUUID))
                return false;
            String globalCachedData = pipelineManager.getGlobalCache().loadData(dataClass, objectUUID);
            // Error while loading from redis
            if (globalCachedData == null) {
                System.out.println("Trying to load from storage...");
                doSynchronisation(DataSourceType.GLOBAL_STORAGE, DataSourceType.LOCAL, dataClass, objectUUID, callback);
                return false;
            }

            if (destination.equals(DataSourceType.LOCAL)) {
                if (!pipelineManager.getLocalCache().dataExist(dataClass, objectUUID)) {
                    pipelineManager.getLocalCache().save(dataClass, pipelineManager.getLocalCache().instantiateData(pipelineManager, dataClass, objectUUID));
                }

                PipelineData data = pipelineManager.getLocalCache().getData(dataClass, objectUUID);
                if (data == null)
                    return false;
                data.deserialize(globalCachedData);
                data.updateLastUse();
                data.loadDependentData();
                data.onLoad();
                pipelineManager.getLocalCache().save(dataClass, data);
            } else if (destination.equals(DataSourceType.GLOBAL_STORAGE))
                pipelineManager.getGlobalStorage().saveData(dataClass, objectUUID, globalCachedData);

        } else if (source.equals(DataSourceType.GLOBAL_STORAGE)) {
            if (!pipelineManager.getGlobalStorage().dataExist(dataClass, objectUUID))
                return false;
            String globalSavedData = pipelineManager.getGlobalStorage().loadData(dataClass, objectUUID);

            if (destination.equals(DataSourceType.LOCAL)) {
                if (!pipelineManager.getLocalCache().dataExist(dataClass, objectUUID)) {
                    pipelineManager.getLocalCache().save(dataClass, pipelineManager.getLocalCache().instantiateData(pipelineManager, dataClass, objectUUID));
                }

                PipelineData data = pipelineManager.getLocalCache().getData(dataClass, objectUUID);
                if (data == null)
                    return false;
                data.deserialize(globalSavedData);
                data.updateLastUse();
                data.loadDependentData();
                data.onLoad();
                pipelineManager.getLocalCache().save(dataClass, data);
            } else if (destination.equals(DataSourceType.GLOBAL_CACHE))
                pipelineManager.getGlobalCache().saveData(dataClass, objectUUID, globalSavedData);
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
            pipelineManager.getExecutorService().shutdown();
            pipelineManager.getExecutorService().awaitTermination(5, TimeUnit.SECONDS);
            System.out.println("Data Synchronizer shut down successfully");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
