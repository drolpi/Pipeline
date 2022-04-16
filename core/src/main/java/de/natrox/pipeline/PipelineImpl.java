package de.natrox.pipeline;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.common.runnable.CatchingRunnable;
import de.natrox.common.scheduler.Scheduler;
import de.natrox.pipeline.annotation.property.Context;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.cleanup.CleanUpTask;
import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.connection.ConnectionData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import de.natrox.pipeline.operator.PipelineStream;
import de.natrox.pipeline.operator.PipelineStreamImpl;
import de.natrox.pipeline.part.DataSynchronizer;
import de.natrox.pipeline.part.DataSynchronizerImpl;
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.cache.GlobalCacheProvider;
import de.natrox.pipeline.part.local.DefaultLocalCache;
import de.natrox.pipeline.part.local.LocalCache;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import de.natrox.pipeline.part.updater.DataUpdater;
import de.natrox.pipeline.part.updater.DataUpdaterProvider;
import de.natrox.pipeline.part.updater.DefaultDataUpdater;
import de.natrox.pipeline.scheduler.PipelineTaskScheduler;
import de.natrox.pipeline.scheduler.PipelineTaskSchedulerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class PipelineImpl implements Pipeline {

    private final static Logger LOGGER = LogManager.logger(PipelineImpl.class);

    private final GlobalStorage globalStorage;
    private final GlobalCache globalCache;
    private final DataUpdater dataUpdater;
    private final LocalCache localCache;
    private final PipelineRegistry registry;
    private final DataSynchronizerImpl pipelineDataSynchronizer;
    private final PipelineTaskScheduler pipelineTaskScheduler;
    private final ExecutorService executorService;
    private final Scheduler scheduler;
    private final Gson gson;
    private final boolean loaded;

    protected PipelineImpl(
        @Nullable DataUpdaterProvider dataUpdaterProvider,
        @Nullable GlobalCacheProvider globalCacheProvider,
        @Nullable GlobalStorageProvider globalStorageProvider,
        @NotNull PipelineRegistry registry
    ) throws Exception {
        this.registry = registry;
        this.executorService = Executors.newFixedThreadPool(4);
        this.gson = new GsonBuilder().serializeNulls().create();
        this.localCache = new DefaultLocalCache();

        LOGGER.debug("Pipeline information:");
        LOGGER.debug("Local cache: " + localCache.getClass().getName());

        // check if there is a data updater provider or initialize the default one
        if (dataUpdaterProvider != null && dataUpdaterProvider.init()) {
            this.dataUpdater = dataUpdaterProvider.constructDataUpdater(this);
        } else {
            this.dataUpdater = new DefaultDataUpdater();
        }

        LOGGER.debug("Data updater: " + dataUpdater.getClass().getName());

        // check if there is a global cache provider or initialize null
        if (globalCacheProvider != null && globalCacheProvider.init()) {
            this.globalCache = globalCacheProvider.constructGlobalCache(this);
            LOGGER.debug("GlobalCache: " + globalCache.getClass().getName());
        } else {
            this.globalCache = null;
            LOGGER.debug("No global cache found");
        }

        // check if there is a global storage provider or or initialize null
        if (globalStorageProvider != null && globalStorageProvider.init()) {
            this.globalStorage = globalStorageProvider.constructGlobalStorage(this);
            LOGGER.debug("Global storage: " + globalStorage.getClass().getName());
        } else {
            this.globalStorage = null;
            LOGGER.debug("No global storage found");
        }

        this.scheduler = Scheduler.create();
        this.pipelineTaskScheduler = new PipelineTaskSchedulerImpl();
        this.pipelineDataSynchronizer = new DataSynchronizerImpl(this);
        this.scheduler
            .buildTask(new CleanUpTask(this))
            .repeat(300, TimeUnit.SECONDS)
            .schedule();
        loaded = true;
    }

    @Override
    public @NotNull <T extends PipelineData> PipelineStream<T> find(
        @NotNull Class<? extends T> dataClass,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        return new PipelineStreamImpl<>(this, dataClass, loadingStrategy, callback, instanceCreator);
    }

    @Override
    public final <T extends PipelineData> @NotNull Optional<T> load(
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID objectUUID,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator,
        boolean createIfNotExists
    ) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        registry.checkRegistered(dataClass);

        PipelineTaskScheduler.PipelineTask<T> pipelineTask = pipelineTaskScheduler
            .schedule(PipelineTaskScheduler.PipelineAction.LOAD, loadingStrategy, dataClass, objectUUID);

        if (localCache.dataExist(dataClass, objectUUID)) {
            T data = localCache.data(dataClass, objectUUID);
            if (callback != null)
                callback.accept(data);
            if (data != null)
                data.updateLastUse();
            pipelineTask.completableFuture().complete(data);
            return Optional.ofNullable(data);
        } else if (loadingStrategy.equals(LoadingStrategy.LOAD_LOCAL)) {
            if (createIfNotExists) {
                T data = createNewData(dataClass, objectUUID, instanceCreator);
                data.updateLastUse();
                pipelineTask.completableFuture().complete(data);
                if (callback != null)
                    callback.accept(data);
                return Optional.ofNullable(data);
            }
            pipelineTask.completableFuture().complete(null);
        } else if (loadingStrategy.equals(LoadingStrategy.LOAD_LOCAL_ELSE_LOAD)) {
            executorService.submit(new CatchingRunnable(() -> {
                T data = loadFromPipeline(dataClass, objectUUID, instanceCreator, createIfNotExists);
                pipelineTask.completableFuture().complete(data);
                LOGGER.debug("[" + loadingStrategy + "] Completed with: " + data);
                if (callback != null)
                    callback.accept(data);
            }));
            return null;
        } else if (loadingStrategy.equals(LoadingStrategy.LOAD_PIPELINE)) {
            T data = loadFromPipeline(dataClass, objectUUID, instanceCreator, createIfNotExists);
            pipelineTask.completableFuture().complete(data);
            LOGGER.debug("[" + loadingStrategy + "] Completed with: " + data);
            if (callback != null)
                callback.accept(data);

            return Optional.ofNullable(data);
        }
        return Optional.empty();
    }

    @NotNull
    @Override
    public <T extends PipelineData> CompletableFuture<Optional<T>> loadAsync(
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID objectUUID,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator,
        boolean createIfNotExists
    ) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        registry.checkRegistered(dataClass);

        var completableFuture = new CompletableFuture<Optional<T>>();
        executorService.submit(new CatchingRunnable(() ->
            completableFuture.complete(load(dataClass, objectUUID, loadingStrategy, callback, instanceCreator, createIfNotExists))));
        return completableFuture;
    }

    @Override
    public @NotNull <T extends PipelineData> List<T> load(
        @NotNull Class<? extends T> dataClass,
        @NotNull Iterable<UUID> objectUUIDs,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUIDs, "objectUUIDs");
        registry.checkRegistered(dataClass);

        if (loadingStrategy.equals(LoadingStrategy.LOAD_PIPELINE))
            synchronizeData(dataClass, objectUUIDs, instanceCreator);
        else if (loadingStrategy.equals(LoadingStrategy.LOAD_LOCAL_ELSE_LOAD))
            executorService.submit(new CatchingRunnable(() -> synchronizeData(dataClass, objectUUIDs, instanceCreator)));

        return localCache.savedUUIDs(dataClass)
            .stream()
            .map(uuid -> localCache.data(dataClass, uuid))
            .collect(Collectors.toList());
    }

    @Override
    public @NotNull <T extends PipelineData> CompletableFuture<List<T>> loadAsync(
        @NotNull Class<? extends T> dataClass,
        @NotNull Iterable<UUID> objectUUIDs,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUIDs, "objectUUIDs");
        registry.checkRegistered(dataClass);

        var completableFuture = new CompletableFuture<List<T>>();
        executorService.submit(new CatchingRunnable(() -> completableFuture.complete(load(dataClass, objectUUIDs, loadingStrategy))));
        return completableFuture;
    }

    @Override
    public <T extends PipelineData> boolean exist(
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID objectUUID,
        @NotNull QueryStrategy... strategies
    ) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        registry.checkRegistered(dataClass);

        if (strategies.length == 0)
            return false;
        var strategySet = Arrays.stream(strategies).collect(Collectors.toSet());

        if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.LOCAL)) {
            var localExist = localCache().dataExist(dataClass, objectUUID);
            if (localExist)
                return true;
        }
        if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_CACHE)) {
            if (globalCache != null) {
                var globalCacheExists = globalCache.dataExist(dataClass, objectUUID);
                if (globalCacheExists)
                    return true;
            }
        }
        if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_STORAGE)) {
            if (globalStorage != null)
                return globalStorage.dataExist(dataClass, objectUUID);
        }
        return false;
    }

    @NotNull
    @Override
    public <T extends PipelineData> CompletableFuture<Boolean> existAsync(
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID objectUUID,
        @NotNull QueryStrategy... strategies
    ) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        registry.checkRegistered(dataClass);

        var completableFuture = new CompletableFuture<Boolean>();
        executorService.submit(new CatchingRunnable(() -> completableFuture.complete(exist(dataClass, objectUUID, strategies))));
        return completableFuture;
    }

    @Override
    public <T extends PipelineData> boolean delete(
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID objectUUID,
        boolean notifyOthers,
        @NotNull QueryStrategy... strategies
    ) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        registry.checkRegistered(dataClass);

        var strategySet = Arrays.stream(strategies).collect(Collectors.toSet());
        if (strategySet.isEmpty())
            strategySet.add(QueryStrategy.ALL);
        LOGGER.debug("Deleting: " + dataClass.getSimpleName() + " uuid " + objectUUID + "" + Arrays.toString(strategies));
        if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.LOCAL)) {
            LOGGER.debug("Deleting from Local Cache: " + dataClass.getSimpleName() + " uuid " + objectUUID);
            T data = localCache().data(dataClass, objectUUID);

            if (data != null) {
                data.onDelete();
                data.onCleanUp();
            }
            if (!localCache().remove(dataClass, objectUUID))
                LOGGER.debug("[LocalCache] Could not delete: " + dataClass.getSimpleName() + " uuid " + objectUUID);
            else if (data != null) {
                if (notifyOthers)
                    data.dataUpdater().pushRemoval(data, null);
                data.markForRemoval();
                LOGGER.debug("[LocalCache] Deleted: " + dataClass.getSimpleName() + " uuid " + objectUUID + "" + Arrays.toString(strategies));
            }
        }
        if (globalCache != null && (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_CACHE))) {
            LOGGER.debug("Deleting from Global Cache: " + dataClass.getSimpleName() + " uuid " + objectUUID + "");
            if (!globalCache.removeData(dataClass, objectUUID))
                LOGGER.debug("[GlobalCache] Could not delete: " + dataClass.getSimpleName() + " uuid " + objectUUID);
            else
                LOGGER.debug("[GlobalCache] Deleted: " + dataClass.getSimpleName() + " uuid " + objectUUID + "" + Arrays.toString(strategies));
        }
        if (globalStorage != null && (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_STORAGE))) {
            LOGGER.debug("Deleting from Global Storage: " + dataClass.getSimpleName() + " uuid " + objectUUID + "");
            if (!globalStorage.removeData(dataClass, objectUUID))
                LOGGER.debug("[GlobalStorage] Could not delete: " + dataClass.getSimpleName() + " uuid " + objectUUID);
            else
                LOGGER.debug("[GlobalStorage] Deleted: " + dataClass.getSimpleName() + " uuid " + objectUUID + "" + Arrays.toString(strategies));
        }
        return true;
    }

    @NotNull
    @Override
    public <T extends PipelineData> CompletableFuture<Boolean> deleteAsync(
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID objectUUID,
        boolean notifyOthers,
        @NotNull QueryStrategy... strategies
    ) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        registry.checkRegistered(dataClass);

        var completableFuture = new CompletableFuture<Boolean>();
        executorService.submit(new CatchingRunnable(() -> completableFuture.complete(delete(dataClass, objectUUID, notifyOthers, strategies))));
        return completableFuture;
    }

    @NotNull
    @Override
    public LocalCache localCache() {
        return localCache;
    }

    @NotNull
    @Override
    public DataUpdater dataUpdater() {
        return dataUpdater;
    }

    @Nullable
    @Override
    public GlobalCache globalCache() {
        return globalCache;
    }

    @Nullable
    @Override
    public GlobalStorage globalStorage() {
        return globalStorage;
    }

    @Override
    public void cleanUpAllData() {
        LOGGER.debug("Saving all data...");
        registry.dataClasses().forEach(this::cleanUpData);
    }

    @Override
    public void preloadAllData() {
        LOGGER.debug("Preloading all data...");
        registry.dataClasses().stream().filter(aClass -> !ConnectionData.class.isAssignableFrom(aClass)).forEach(this::preloadData);
    }

    @Override
    public void preloadData(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        registry.checkRegistered(dataClass);

        //Connection
        if (ConnectionData.class.isAssignableFrom(dataClass))
            return;
        var optional = AnnotationResolver.preload(dataClass);

        //Data will only be preloaded if it is declared properly
        optional.ifPresent(preload -> {
            var startTime = System.currentTimeMillis();
            LOGGER.debug("Preloading " + dataClass.getSimpleName());
            if (globalCache != null)
                globalCache.savedUUIDs(dataClass).forEach(uuid -> preloadData(dataClass, uuid));
            if (globalStorage != null)
                globalStorage.savedUUIDs(dataClass).forEach(uuid -> preloadData(dataClass, uuid));
            LOGGER.debug("Done preloading " + dataClass.getSimpleName() + " in " + (System.currentTimeMillis() - startTime) + "ms");
        });
    }

    @Override
    public void preloadData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        registry.checkRegistered(dataClass);

        //Connection
        if (ConnectionData.class.isAssignableFrom(dataClass))
            return;
        if (localCache.dataExist(dataClass, objectUUID))
            return;

        pipelineDataSynchronizer.doSynchronisation(
            DataSynchronizer.DataSourceType.GLOBAL_CACHE, DataSynchronizer.DataSourceType.LOCAL, dataClass, objectUUID, null, null
        );

        if (localCache.dataExist(dataClass, objectUUID))
            return;

        pipelineDataSynchronizer.doSynchronisation(
            DataSynchronizer.DataSourceType.GLOBAL_STORAGE, DataSynchronizer.DataSourceType.LOCAL, dataClass, objectUUID, null, null
        );

        if (AnnotationResolver.context(dataClass).equals(Context.GLOBAL) && globalCache != null)
            pipelineDataSynchronizer.doSynchronisation(
                DataSynchronizer.DataSourceType.GLOBAL_STORAGE, DataSynchronizer.DataSourceType.GLOBAL_CACHE, dataClass, objectUUID, null, null
            );
    }

    @Override
    public void cleanUpData(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        registry.checkRegistered(dataClass);

        var optional = AnnotationResolver.autoSave(dataClass);

        // Data will only be preloaded if it is declared properly
        optional.ifPresent(unload -> {
            var startTime = System.currentTimeMillis();
            LOGGER.debug("Saving " + dataClass.getSimpleName());
            localCache.savedUUIDs(dataClass).forEach(uuid -> cleanUpData(dataClass, uuid, null));
            LOGGER.debug("Done saving " + dataClass.getSimpleName() + " in " + (System.currentTimeMillis() - startTime) + "ms");
        });
    }

    @Override
    public void cleanUpData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, Runnable runnable) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        registry.checkRegistered(dataClass);

        var optional = AnnotationResolver.autoSave(dataClass);

        optional.ifPresent(autoSave -> {
            var pipelineData = localCache().data(dataClass, objectUUID);
            LOGGER.debug("Saving " + objectUUID + " [" + dataClass + "]");
            if (pipelineData == null)
                return;
            if (pipelineData.isMarkedForRemoval())
                return;
            pipelineData.onCleanUp();

            if (pipelineData instanceof ConnectionData connectionData)
                connectionData.onDisconnect();

            pipelineData.save(() -> {
                localCache.remove(dataClass, pipelineData.objectUUID());
                if (runnable != null)
                    runnable.run();
            });
        });
    }

    @NotNull
    @Override
    public DataSynchronizer dataSynchronizer() {
        return pipelineDataSynchronizer;
    }

    private <T extends PipelineData> T loadFromPipeline(
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID objectUUID,
        @Nullable InstanceCreator<T> instanceCreator,
        boolean createIfNotExists
    ) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        registry.checkRegistered(dataClass);

        var startTime = System.currentTimeMillis();
        // ExistCheck LocalCache
        if (localCache.dataExist(dataClass, objectUUID))
            LOGGER.debug("Found Data in Local Cache [" + dataClass.getSimpleName() + "]");
            // ExistCheck GlobalCache
        else {
            boolean globalCacheExists = pipelineDataSynchronizer.doSynchronisation(
                DataSynchronizer.DataSourceType.GLOBAL_CACHE, DataSynchronizer.DataSourceType.LOCAL, dataClass, objectUUID, null, instanceCreator
            );

            if (globalCacheExists)
                LOGGER.debug("Found Data in Redis Cache [" + dataClass.getSimpleName() + "]");
            else {
                boolean globalStorageExists = pipelineDataSynchronizer.doSynchronisation(
                    DataSynchronizer.DataSourceType.GLOBAL_STORAGE, DataSynchronizer.DataSourceType.LOCAL, dataClass, objectUUID, null, instanceCreator
                );

                if (globalStorageExists) {
                    LOGGER.debug("Found Data in Database [" + dataClass.getSimpleName() + "]");
                    if (AnnotationResolver.context(dataClass).equals(Context.GLOBAL))
                        pipelineDataSynchronizer.synchronize(
                            DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_CACHE, dataClass, objectUUID, null, instanceCreator
                        );
                } else {
                    if (!createIfNotExists)
                        return null;
                    T data = createNewData(dataClass, objectUUID, instanceCreator);
                    data.updateLastUse();
                    LOGGER.debug("Done loading in " + (System.currentTimeMillis() - startTime) + "ms");
                    return data;
                }
            }
        }
        LOGGER.debug("Done loading in " + (System.currentTimeMillis() - startTime) + "ms");
        T data = localCache.data(dataClass, objectUUID);
        if (data != null)
            data.updateLastUse();
        else
            LOGGER.warning("Data deleted from other thread while loading " + dataClass.getSimpleName() + " with uuid: " + objectUUID);

        return data;
    }

    private <T extends PipelineData> T createNewData(
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID objectUUID,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        registry.checkRegistered(dataClass);

        LOGGER.debug("No Data was found. Creating new data! [" + dataClass.getSimpleName() + "]");
        T pipelineData = localCache.data(dataClass, objectUUID);

        if (pipelineData == null) {
            pipelineData = localCache.instantiateData(this, dataClass, objectUUID, instanceCreator);
            pipelineData.onCreate();
        }
        pipelineData.loadDependentData();

        localCache.save(dataClass, pipelineData);
        pipelineDataSynchronizer.synchronize(
            DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_CACHE, dataClass, objectUUID, null, instanceCreator
        );
        pipelineDataSynchronizer.synchronize(
            DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_STORAGE, dataClass, objectUUID, null, instanceCreator
        );
        return pipelineData;
    }

    private <T extends PipelineData> void synchronizeData(@NotNull Class<? extends T> dataClass, @NotNull Iterable<UUID> objectUUIDs, InstanceCreator<T> instanceCreator) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUIDs, "objectUUIDs");
        registry.checkRegistered(dataClass);

        if (globalCache() != null) {
            objectUUIDs.forEach(uuid -> {
                if (!localCache.dataExist(dataClass, uuid))
                    pipelineDataSynchronizer.doSynchronisation(
                        DataSynchronizer.DataSourceType.GLOBAL_CACHE, DataSynchronizer.DataSourceType.LOCAL, dataClass, uuid, null, instanceCreator
                    );
            });
        }
        if (globalStorage() != null) {
            var context = AnnotationResolver.context(dataClass);
            objectUUIDs.forEach(uuid -> {
                if (!localCache.dataExist(dataClass, uuid)) {
                    pipelineDataSynchronizer.doSynchronisation(
                        DataSynchronizer.DataSourceType.GLOBAL_STORAGE, DataSynchronizer.DataSourceType.LOCAL, dataClass, uuid, null, instanceCreator
                    );

                    if (context.equals(Context.GLOBAL) && globalCache != null)
                        pipelineDataSynchronizer.synchronize(
                            DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_CACHE, dataClass, uuid, null, instanceCreator
                        );
                }
            });
        }
    }

    @Override
    public void load() {

    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void shutdown() {
        try {
            scheduler.shutdown();
            pipelineDataSynchronizer.shutdown();
            executorService.shutdown();
            pipelineTaskScheduler.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public ExecutorService executorService() {
        return executorService;
    }

    @NotNull
    @Override
    public PipelineRegistry registry() {
        return registry;
    }

    @NotNull
    @Override
    public Gson gson() {
        return gson;
    }
}
