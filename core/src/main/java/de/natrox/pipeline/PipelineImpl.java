package de.natrox.pipeline;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.natrox.common.logger.LogManager;
import de.natrox.common.logger.Logger;
import de.natrox.common.runnable.CatchingRunnable;
import de.natrox.common.scheduler.Scheduler;
import de.natrox.pipeline.annotation.property.Context;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.automatic.cleanup.CleanUpTask;
import de.natrox.pipeline.config.PipelineConfig;
import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.datatype.ConnectionPipelineData;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import de.natrox.pipeline.operator.PipelineStream;
import de.natrox.pipeline.operator.PipelineStreamImpl;
import de.natrox.pipeline.part.DataSynchronizer;
import de.natrox.pipeline.part.DataSynchronizerImpl;
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.local.DefaultLocalCache;
import de.natrox.pipeline.part.local.LocalCache;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.updater.DataUpdaterService;
import de.natrox.pipeline.part.updater.DefaultDataUpdaterService;
import de.natrox.pipeline.scheduler.PipelineTaskScheduler;
import de.natrox.pipeline.scheduler.PipelineTaskSchedulerImpl;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
    private final DataUpdaterService dataUpdaterService;
    private final LocalCache localCache;
    private final PipelineRegistry registry;
    private final DataSynchronizerImpl pipelineDataSynchronizer;
    private final PipelineTaskScheduler pipelineTaskScheduler;
    private final ExecutorService executorService;
    private final Scheduler scheduler;
    private final Gson gson;
    private final boolean loaded;

    public PipelineImpl(@NotNull PipelineRegistry registry, @NotNull PipelineConfig config) {
        this.registry = registry;
        this.executorService = Executors.newFixedThreadPool(4, new DefaultThreadFactory("Pipeline"));
        this.gson = new GsonBuilder().serializeNulls().create();
        this.localCache = new DefaultLocalCache();

        var updaterConfig = config.dataUpdaterConnection();
        if (updaterConfig != null) {
            updaterConfig.load();
            this.dataUpdaterService = updaterConfig.constructDataUpdaterService(this);
        } else {
            this.dataUpdaterService = new DefaultDataUpdaterService();
        }

        var globalCacheConfig = config.globalCacheConnection();
        if (globalCacheConfig != null) {
            globalCacheConfig.load();
            this.globalCache = globalCacheConfig.constructGlobalCache(this);
        } else {
            this.globalCache = null;
        }

        var globalStorageConfig = config.globalStorageConnection();
        if (globalStorageConfig != null) {
            globalStorageConfig.load();
            this.globalStorage = globalStorageConfig.constructGlobalStorage(this);
        } else {
            this.globalStorage = null;
        }

        //LOGGER.info("Pipeline information:");
        //LOGGER.info("LocalCache: " + localCache.getClass().getName());
        //LOGGER.info("DataUpdater: " + dataUpdaterService.getClass().getName());
        //LOGGER.info("GlobalCache: " + globalCache.getClass().getName());
        //LOGGER.info("GlobalStorage: " + globalStorage.getClass().getName());

        this.scheduler = new Scheduler();
        this.pipelineTaskScheduler = new PipelineTaskSchedulerImpl();
        this.pipelineDataSynchronizer = new DataSynchronizerImpl(this);
        scheduler.interval(new CleanUpTask(this), 20L * 10, 20L * 300);
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
    public final <T extends PipelineData> T load(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        boolean createIfNotExists,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.isRegistered(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        PipelineTaskScheduler.PipelineTask<T> pipelineTask = pipelineTaskScheduler
            .schedule(PipelineTaskScheduler.PipelineAction.LOAD, loadingStrategy, type, uuid);

        if (localCache.dataExist(type, uuid)) {
            T data = localCache.data(type, uuid);
            if (callback != null)
                callback.accept(data);
            if (data != null)
                data.updateLastUse();
            pipelineTask.completableFuture().complete(data);
            return data;
        } else if (loadingStrategy.equals(LoadingStrategy.LOAD_LOCAL)) {
            if (createIfNotExists) {
                T data = createNewData(type, uuid, instanceCreator);
                data.updateLastUse();
                pipelineTask.completableFuture().complete(data);
                if (callback != null)
                    callback.accept(data);
                return data;
            }
            pipelineTask.completableFuture().complete(null);
        } else if (loadingStrategy.equals(LoadingStrategy.LOAD_LOCAL_ELSE_LOAD)) {
            executorService.submit(new CatchingRunnable(() -> {
                T data = loadFromPipeline(type, uuid, instanceCreator, createIfNotExists);
                pipelineTask.completableFuture().complete(data);
                //LOGGER.info("[" + loadingStrategy + "] Completed with: " + data); //DEBUG
                if (callback != null)
                    callback.accept(data);
            }));
            return null;
        } else if (loadingStrategy.equals(LoadingStrategy.LOAD_PIPELINE)) {
            T data = loadFromPipeline(type, uuid, instanceCreator, createIfNotExists);
            pipelineTask.completableFuture().complete(data);
            //LOGGER.info("[" + loadingStrategy + "] Completed with: " + data); //DEBUG
            if (callback != null)
                callback.accept(data);

            return data;
        }
        return null;
    }

    @NotNull
    @Override
    public <T extends PipelineData> CompletableFuture<T> loadAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull LoadingStrategy loadingStrategy,
        boolean createIfNotExists,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.isRegistered(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        var completableFuture = new CompletableFuture<T>();
        executorService.submit(new CatchingRunnable(() ->
            completableFuture.complete(load(type, uuid, loadingStrategy, createIfNotExists, callback, instanceCreator))));
        return completableFuture;
    }

    @Override
    public @NotNull <T extends PipelineData> List<T> load(
        @NotNull Class<? extends T> type,
        @NotNull Iterable<UUID> uuids,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuids, "Uuids can't be null");
        if (!registry.isRegistered(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        if (loadingStrategy.equals(LoadingStrategy.LOAD_PIPELINE))
            synchronizeData(type, uuids, instanceCreator);
        else if (loadingStrategy.equals(LoadingStrategy.LOAD_LOCAL_ELSE_LOAD))
            executorService.submit(new CatchingRunnable(() -> synchronizeData(type, uuids, instanceCreator)));

        return localCache.savedUUIDs(type)
            .stream()
            .map(uuid -> localCache.data(type, uuid))
            .collect(Collectors.toList());
    }

    @Override
    public @NotNull <T extends PipelineData> CompletableFuture<List<T>> loadAsync(
        @NotNull Class<? extends T> type,
        @NotNull Iterable<UUID> uuids,
        @NotNull LoadingStrategy loadingStrategy,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        if (!registry.isRegistered(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        var completableFuture = new CompletableFuture<List<T>>();
        executorService.submit(new CatchingRunnable(() -> completableFuture.complete(load(type, uuids, loadingStrategy))));
        return completableFuture;
    }

    @Override
    public <T extends PipelineData> boolean exist(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull QueryStrategy... strategies
    ) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.isRegistered(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        if (strategies.length == 0)
            return false;
        var strategySet = Arrays.stream(strategies).collect(Collectors.toSet());

        if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.LOCAL)) {
            var localExist = localCache().dataExist(type, uuid);
            if (localExist)
                return true;
        }
        if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_CACHE)) {
            if (globalCache != null) {
                var globalCacheExists = globalCache.dataExist(type, uuid);
                if (globalCacheExists)
                    return true;
            }
        }
        if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_STORAGE)) {
            if (globalStorage != null)
                return globalStorage.dataExist(type, uuid);
        }
        return false;
    }

    @NotNull
    @Override
    public <T extends PipelineData> CompletableFuture<Boolean> existAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        @NotNull QueryStrategy... strategies
    ) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.isRegistered(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        var completableFuture = new CompletableFuture<Boolean>();
        executorService.submit(new CatchingRunnable(() -> completableFuture.complete(exist(type, uuid, strategies))));
        return completableFuture;
    }

    @Override
    public <T extends PipelineData> boolean delete(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        boolean notifyOthers,
        @NotNull QueryStrategy... strategies
    ) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.isRegistered(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        var strategySet = Arrays.stream(strategies).collect(Collectors.toSet());
        if (strategySet.isEmpty())
            strategySet.add(QueryStrategy.ALL);
        //LOGGER.info("Deleting: " + type.getSimpleName() + " uuid " + uuid + "" + Arrays.toString(strategies)); //DEBUG
        if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.LOCAL)) {
            //LOGGER.info("Deleting from Local Cache: " + type.getSimpleName() + " uuid " + uuid); //DEBUG
            T data = localCache().data(type, uuid);

            if (data != null) {
                data.onDelete();
                data.onCleanUp();
            }
            if (!localCache().remove(type, uuid))
                new String(); //REMOVE
                //LOGGER.info("[LocalCache] Could not delete: " + type.getSimpleName() + " uuid " + uuid); //DEBUG
            else if (data != null) {
                if (notifyOthers)
                    data.dataUpdater().pushRemoval(data, null);
                data.markForRemoval();
                //LOGGER.info("[LocalCache] Deleted: " + type.getSimpleName() + " uuid " + uuid + "" + Arrays.toString(strategies)); //DEBUG
            }
        }
        if (globalCache != null && (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_CACHE))) {
            //LOGGER.info("Deleting from Global Cache: " + type.getSimpleName() + " uuid " + uuid + ""); //DEBUG
            if (!globalCache.removeData(type, uuid))
                new String();
                //LOGGER.info("[GlobalCache] Could not delete: " + type.getSimpleName() + " uuid " + uuid); //DEBUG
            else
                new String();
                //LOGGER.info("[GlobalCache] Deleted: " + type.getSimpleName() + " uuid " + uuid + "" + Arrays.toString(strategies)); //DEBUG
        }
        if (globalStorage != null && (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_STORAGE))) {
            //LOGGER.info("Deleting from Global Storage: " + type.getSimpleName() + " uuid " + uuid + ""); //DEBUG
            if (!globalStorage.removeData(type, uuid))
                new String();
                //LOGGER.info("[GlobalStorage] Could not delete: " + type.getSimpleName() + " uuid " + uuid); //DEBUG
            else
                new String();
                //LOGGER.info("[GlobalStorage] Deleted: " + type.getSimpleName() + " uuid " + uuid + "" + Arrays.toString(strategies)); //DEBUG
        }
        return true;
    }

    @NotNull
    @Override
    public <T extends PipelineData> CompletableFuture<Boolean> deleteAsync(
        @NotNull Class<? extends T> type,
        @NotNull UUID uuid,
        boolean notifyOthers,
        @NotNull QueryStrategy... strategies
    ) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.isRegistered(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");
        var completableFuture = new CompletableFuture<Boolean>();
        executorService.submit(new CatchingRunnable(() -> completableFuture.complete(delete(type, uuid, notifyOthers, strategies))));
        return completableFuture;
    }

    @NotNull
    @Override
    public LocalCache localCache() {
        return localCache;
    }

    @NotNull
    @Override
    public DataUpdaterService dataUpdaterService() {
        return dataUpdaterService;
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
        //LOGGER.info("Saving all data...");
        registry.dataClasses().forEach(this::cleanUpData);
    }

    @Override
    public void preloadAllData() {
        //LOGGER.info("Preloading all data...");
        registry.dataClasses().stream().filter(aClass -> !ConnectionPipelineData.class.isAssignableFrom(aClass)).forEach(this::preloadData);
    }

    @Override
    public void preloadData(@NotNull Class<? extends PipelineData> type) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        if (!registry.isRegistered(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        //Connection
        if (ConnectionPipelineData.class.isAssignableFrom(type))
            return;
        var optional = AnnotationResolver.preload(type);

        //Data will only be preloaded if it is declared properly
        optional.ifPresent(preload -> {
            var startTime = System.currentTimeMillis();
            //LOGGER.info("Preloading " + type.getSimpleName()); //DEBUG
            if (globalCache != null)
                globalCache.savedUUIDs(type).forEach(uuid -> preloadData(type, uuid));
            if (globalStorage != null)
                globalStorage.savedUUIDs(type).forEach(uuid -> preloadData(type, uuid));
            //LOGGER.info("Done preloading " + type.getSimpleName() + " in " + (System.currentTimeMillis() - startTime) + "ms"); //DEBUG
        });
    }

    @Override
    public void preloadData(@NotNull Class<? extends PipelineData> type, @NotNull UUID uuid) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(uuid);
        if (!registry.isRegistered(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        //Connection
        if (ConnectionPipelineData.class.isAssignableFrom(type))
            return;
        if (localCache.dataExist(type, uuid))
            return;

        pipelineDataSynchronizer.doSynchronisation(
            DataSynchronizer.DataSourceType.GLOBAL_CACHE, DataSynchronizer.DataSourceType.LOCAL, type, uuid, null, null
        );

        if (localCache.dataExist(type, uuid))
            return;

        pipelineDataSynchronizer.doSynchronisation(
            DataSynchronizer.DataSourceType.GLOBAL_STORAGE, DataSynchronizer.DataSourceType.LOCAL, type, uuid, null, null
        );

        if (AnnotationResolver.context(type).equals(Context.GLOBAL) && globalCache != null)
            pipelineDataSynchronizer.doSynchronisation(
                DataSynchronizer.DataSourceType.GLOBAL_STORAGE, DataSynchronizer.DataSourceType.GLOBAL_CACHE, type, uuid, null, null
            );
    }

    @Override
    public void cleanUpData(@NotNull Class<? extends PipelineData> type) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        if (!registry.isRegistered(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        var optional = AnnotationResolver.autoSave(type);

        // Data will only be preloaded if it is declared properly
        optional.ifPresent(unload -> {
            var startTime = System.currentTimeMillis();
            //LOGGER.info("Saving " + type.getSimpleName()); //DEBUG
            localCache.savedUUIDs(type).forEach(uuid -> cleanUpData(type, uuid, null));
            //LOGGER.info("Done saving " + type.getSimpleName() + " in " + (System.currentTimeMillis() - startTime) + "ms"); //DEBUG
        });
    }

    @Override
    public void cleanUpData(@NotNull Class<? extends PipelineData> type, @NotNull UUID uuid, Runnable runnable) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(uuid);
        if (!registry.isRegistered(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        var pipelineData = localCache().data(type, uuid);
        //LOGGER.info("Saving " + uuid + " [" + type + "]"); //DEBUG
        if (pipelineData == null)
            return;
        if (pipelineData.isMarkedForRemoval())
            return;
        pipelineData.onCleanUp();

        var optional = AnnotationResolver.autoSave(type);

        optional.ifPresent(autoSave -> pipelineData.save(() -> {
            localCache.remove(type, pipelineData.objectUUID());
            if (runnable != null)
                runnable.run();
        }));
    }

    @NotNull
    @Override
    public DataSynchronizer dataSynchronizer() {
        return pipelineDataSynchronizer;
    }

    private <T extends PipelineData> T loadFromPipeline(
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID uuid,
        @Nullable InstanceCreator<T> instanceCreator,
        boolean createIfNotExists
    ) {
        Objects.requireNonNull(dataClass, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.dataClasses().contains(dataClass))
            throw new IllegalStateException("The class " + dataClass.getSimpleName() + " is not registered in the pipeline");
        var startTime = System.currentTimeMillis();
        // ExistCheck LocalCache
        if (localCache.dataExist(dataClass, uuid))
            new String();
            //LOGGER.info("Found Data in Local Cache [" + dataClass.getSimpleName() + "]"); //DEBUG
        // ExistCheck GlobalCache
        else {
            boolean globalCacheExists = pipelineDataSynchronizer.doSynchronisation(
                DataSynchronizer.DataSourceType.GLOBAL_CACHE, DataSynchronizer.DataSourceType.LOCAL, dataClass, uuid, null, instanceCreator
            );

            if (globalCacheExists)
                new String();
                //LOGGER.info("Found Data in Redis Cache [" + dataClass.getSimpleName() + "]"); //DEBUG
            else {
                boolean globalStorageExists = pipelineDataSynchronizer.doSynchronisation(
                    DataSynchronizer.DataSourceType.GLOBAL_STORAGE, DataSynchronizer.DataSourceType.LOCAL, dataClass, uuid, null, instanceCreator
                );

                if (globalStorageExists) {
                    //LOGGER.info("Found Data in Database [" + dataClass.getSimpleName() + "]"); //DEBUG
                    if (AnnotationResolver.context(dataClass).equals(Context.GLOBAL))
                        pipelineDataSynchronizer.synchronize(
                            DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_CACHE, dataClass, uuid, null, instanceCreator
                        );
                } else {
                    if (!createIfNotExists)
                        return null;
                    T data = createNewData(dataClass, uuid, instanceCreator);
                    data.updateLastUse();
                    //LOGGER.info("Done loading in " + (System.currentTimeMillis() - startTime) + "ms"); //DEBUG
                    return data;
                }
            }
        }
        //LOGGER.info("Done loading in " + (System.currentTimeMillis() - startTime) + "ms"); //DEBUG
        T data = localCache.data(dataClass, uuid);
        if (data != null)
            data.updateLastUse();
        else
            LOGGER.warning("Data deleted from other thread while loading " + dataClass.getSimpleName() + " with uuid: " + uuid);

        return data;
    }

    private <T extends PipelineData> T createNewData(
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID uuid,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        Objects.requireNonNull(dataClass, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.dataClasses().contains(dataClass))
            throw new IllegalStateException("The class " + dataClass.getSimpleName() + " is not registered in the pipeline");

        //LOGGER.info("No Data was found. Creating new data! [" + dataClass.getSimpleName() + "]"); //DEBUG
        T pipelineData = localCache.data(dataClass, uuid);

        if (pipelineData == null) {
            pipelineData = localCache.instantiateData(this, dataClass, uuid, instanceCreator);
            pipelineData.onCreate();
        }
        pipelineData.loadDependentData();

        localCache.save(dataClass, pipelineData);
        pipelineDataSynchronizer.synchronize(
            DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_CACHE, dataClass, uuid, null, instanceCreator
        );
        pipelineDataSynchronizer.synchronize(
            DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_STORAGE, dataClass, uuid, null, instanceCreator
        );
        return pipelineData;
    }

    private <T extends PipelineData> void synchronizeData(@NotNull Class<? extends T> type, @NotNull Iterable<UUID> uuids, InstanceCreator<T> instanceCreator) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        if (!registry.isRegistered(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");
        if (globalCache() != null) {
            uuids.forEach(uuid -> {
                if (!localCache.dataExist(type, uuid))
                    pipelineDataSynchronizer.doSynchronisation(
                        DataSynchronizer.DataSourceType.GLOBAL_CACHE, DataSynchronizer.DataSourceType.LOCAL, type, uuid, null, instanceCreator
                    );
            });
        }
        if (globalStorage() != null) {
            var context = AnnotationResolver.context(type);
            uuids.forEach(uuid -> {
                if (!localCache.dataExist(type, uuid)) {
                    pipelineDataSynchronizer.doSynchronisation(
                        DataSynchronizer.DataSourceType.GLOBAL_STORAGE, DataSynchronizer.DataSourceType.LOCAL, type, uuid, null, instanceCreator
                    );

                    if (context.equals(Context.GLOBAL) && globalCache != null)
                        pipelineDataSynchronizer.synchronize(
                            DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_CACHE, type, uuid, null, instanceCreator
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
        scheduler.waitUntilShutdown();
        pipelineDataSynchronizer.shutdown();
        executorService.shutdown();
        pipelineTaskScheduler.shutdown();
        try {
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
