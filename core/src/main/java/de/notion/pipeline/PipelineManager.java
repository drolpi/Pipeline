package de.notion.pipeline;

import de.notion.common.runnable.CatchingRunnable;
import de.notion.common.scheduler.Scheduler;
import de.notion.pipeline.annotation.Context;
import de.notion.pipeline.annotation.resolver.AnnotationResolver;
import de.notion.pipeline.automatic.cleanup.AutoCleanUpTask;
import de.notion.pipeline.config.PipelineConfig;
import de.notion.pipeline.datatype.ConnectionPipelineData;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.PipelineDataSynchronizer;
import de.notion.pipeline.part.PipelineDataSynchronizerImpl;
import de.notion.pipeline.part.cache.GlobalCache;
import de.notion.pipeline.part.local.LocalCache;
import de.notion.pipeline.part.local.def.DefaultLocalCache;
import de.notion.pipeline.part.local.updater.DataUpdaterService;
import de.notion.pipeline.part.local.updater.def.DefaultDataUpdaterService;
import de.notion.pipeline.part.storage.GlobalStorage;
import de.notion.pipeline.registry.PipelineRegistry;
import de.notion.pipeline.scheduler.PipelineTaskScheduler;
import de.notion.pipeline.scheduler.PipelineTaskSchedulerImpl;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

public class PipelineManager implements Pipeline {

    private final GlobalStorage globalStorage;
    private final GlobalCache globalCache;
    private final DataUpdaterService dataUpdaterService;
    private final LocalCache localCache;
    private final PipelineRegistry registry;
    private final PipelineDataSynchronizerImpl pipelineDataSynchronizer;
    private final PipelineTaskScheduler pipelineTaskScheduler;
    private final ExecutorService executorService;
    private final Scheduler scheduler;
    private final boolean loaded;

    public PipelineManager(@NotNull PipelineRegistry registry, @NotNull PipelineConfig config) {
        this.executorService = Executors.newFixedThreadPool(2, new DefaultThreadFactory("Pipeline"));
        this.localCache = new DefaultLocalCache();

        var updaterConfig = config.updaterConfig();
        if (updaterConfig != null) {
            updaterConfig.load();
            this.dataUpdaterService = updaterConfig.constructDataManipulator(localCache);
        } else {
            this.dataUpdaterService = new DefaultDataUpdaterService();
        }

        var globalCacheConfig = config.globalCacheConfig();
        if (globalCacheConfig != null) {
            globalCacheConfig.load();
            this.globalCache = globalCacheConfig.constructGlobalCache();
        } else {
            this.globalCache = null;
        }

        var globalStorageConfig = config.globalStorageConfig();
        if (globalStorageConfig != null) {
            globalStorageConfig.load();
            this.globalStorage = globalStorageConfig.constructGlobalStorage();
        } else {
            this.globalStorage = null;
        }

        this.registry = registry;

        System.out.println("Starting Pipeline Manager");
        System.out.println("LocalCache: " + localCache);
        System.out.println("DataUpdater: " + dataUpdaterService);
        System.out.println("GlobalCache: " + globalCache);
        System.out.println("GlobalStorage: " + globalStorage);

        this.scheduler = new Scheduler();
        this.pipelineTaskScheduler = new PipelineTaskSchedulerImpl();
        this.pipelineDataSynchronizer = new PipelineDataSynchronizerImpl(this);
        scheduler.interval(new AutoCleanUpTask(this), 20L * 10, 20L * 300);
        loaded = true;
    }

    @Override
    public final <T extends PipelineData> T load(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull LoadingStrategy loadingStrategy, @Nullable Consumer<T> callback, @NotNull QueryStrategy... creationStrategies) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        //System.out.println("[" + loadingStrategy + "] Loading data from pipeline " + type.getSimpleName() + " " + uuid); //DEBUG
        PipelineTaskScheduler.PipelineTask<T> pipelineTask = pipelineTaskScheduler.schedule(PipelineTaskScheduler.PipelineAction.LOAD, loadingStrategy, type, uuid);

        if (localCache.dataExist(type, uuid)) {
            T data = localCache.data(type, uuid);
            if (callback != null)
                callback.accept(data);
            if (data != null)
                data.updateLastUse();
            pipelineTask.completableFuture().complete(data);
            return data;
        } else if (loadingStrategy.equals(LoadingStrategy.LOAD_LOCAL)) {
            if (creationStrategies.length > 0) {
                T data = createNewData(type, uuid, creationStrategies);
                data.updateLastUse();
                pipelineTask.completableFuture().complete(data);
                if (callback != null)
                    callback.accept(data);
                return data;
            }
            pipelineTask.completableFuture().complete(null);
        } else if (loadingStrategy.equals(LoadingStrategy.LOAD_LOCAL_ELSE_LOAD)) {
            executorService.submit(new CatchingRunnable(() -> {
                T data = loadFromPipeline(type, uuid, creationStrategies);
                pipelineTask.completableFuture().complete(data);
                System.out.println("[" + loadingStrategy + "] Completed with: " + data);
                if (callback != null)
                    callback.accept(data);
            }));
            return null;
        } else if (loadingStrategy.equals(LoadingStrategy.LOAD_PIPELINE)) {
            T data = loadFromPipeline(type, uuid, creationStrategies);
            pipelineTask.completableFuture().complete(data);
            System.out.println("[" + loadingStrategy + "] Completed with: " + data);
            if (callback != null)
                callback.accept(data);
            return data;
        }
        return null;
    }

    @NotNull
    @Override
    public <T extends PipelineData> CompletableFuture<T> loadAsync(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull LoadingStrategy loadingStrategy, @Nullable Consumer<T> callback, @NotNull QueryStrategy... creationStrategies) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        executorService.submit(new CatchingRunnable(() -> completableFuture.complete(load(type, uuid, loadingStrategy, callback, creationStrategies))));
        return completableFuture;
    }

    @Override
    public @NotNull <T extends PipelineData> List<T> loadAllData(@NotNull Class<? extends T> type, @NotNull List<UUID> uuids, @NotNull LoadingStrategy loadingStrategy) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuids, "Uuids can't be null");
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        List<T> set = new ArrayList<>();
        if (loadingStrategy.equals(LoadingStrategy.LOAD_PIPELINE))
            synchronizeData(type, uuids);
        else if (loadingStrategy.equals(LoadingStrategy.LOAD_LOCAL_ELSE_LOAD))
            executorService.submit(new CatchingRunnable(() -> synchronizeData(type, uuids)));
        localCache().savedUUIDs(type).forEach(uuid -> set.add(localCache().data(type, uuid)));
        return set;
    }

    @Override
    public @NotNull <T extends PipelineData> CompletableFuture<List<T>> loadAllDataAsync(@NotNull Class<? extends T> type, @NotNull List<UUID> uuids, @NotNull LoadingStrategy loadingStrategy) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        CompletableFuture<List<T>> completableFuture = new CompletableFuture<>();
        executorService.submit(new CatchingRunnable(() -> completableFuture.complete(loadAllData(type, uuids, loadingStrategy))));
        return completableFuture;
    }

    @NotNull
    @Override
    public <T extends PipelineData> List<T> loadAllData(@NotNull Class<? extends T> type, @NotNull LoadingStrategy loadingStrategy) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        List<UUID> uuids = new ArrayList<>();

        if (globalCache() != null)
            uuids.addAll(globalCache.savedUUIDs(type));

        if (globalStorage() != null)
            uuids.addAll(globalStorage.savedUUIDs(type));

        return loadAllData(type, uuids, loadingStrategy);
    }

    @NotNull
    @Override
    public <T extends PipelineData> CompletableFuture<List<T>> loadAllDataAsync(@NotNull Class<? extends T> type, @NotNull LoadingStrategy loadingStrategy) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        CompletableFuture<List<T>> completableFuture = new CompletableFuture<>();
        executorService.submit(new CatchingRunnable(() -> completableFuture.complete(loadAllData(type, loadingStrategy))));
        return completableFuture;
    }

    @Override
    public <T extends PipelineData> boolean exist(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull QueryStrategy... strategies) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.dataClasses().contains(type))
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
            if (globalCache() != null) {
                var globalCacheExists = globalCache().dataExist(type, uuid);
                if (globalCacheExists)
                    return true;
            }
        }
        if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_STORAGE)) {
            if (globalStorage() != null)
                return globalStorage().dataExist(type, uuid);
        }
        return false;
    }

    @Override
    public <T extends PipelineData> CompletableFuture<Boolean> existAsync(@NotNull Class<? extends T> type, @NotNull UUID uuid, @NotNull QueryStrategy... strategies) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        executorService.submit(new CatchingRunnable(() -> completableFuture.complete(exist(type, uuid, strategies))));
        return completableFuture;
    }

    @Override
    public <T extends PipelineData> boolean delete(@NotNull Class<? extends T> type, @NotNull UUID uuid, boolean notifyOthers, @NotNull QueryStrategy... strategies) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        var strategySet = Arrays.stream(strategies).collect(Collectors.toSet());
        if (strategySet.isEmpty())
            strategySet.add(QueryStrategy.ALL);
        System.out.println("Deleting: " + type.getSimpleName() + " uuid " + uuid + "" + Arrays.toString(strategies)); //DEBUG
        if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.LOCAL)) {
            System.out.println("Deleting from Local Cache: " + type.getSimpleName() + " uuid " + uuid); //DEBUG
            T data = localCache().data(type, uuid);

            if (data != null) {
                data.onDelete();
                data.onCleanUp();
            }
            if (!localCache().remove(type, uuid))
                System.out.println("[LocalCache] Could not delete: " + type.getSimpleName() + " uuid " + uuid); //DEBUG
            else if (data != null) {
                if (notifyOthers)
                    data.dataUpdater().pushRemoval(data, null);
                data.markForRemoval();
                System.out.println("[LocalCache] Deleted: " + type.getSimpleName() + " uuid " + uuid + "" + Arrays.toString(strategies)); //DEBUG
            }
        }
        if (globalCache() != null && (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_CACHE))) {
            System.out.println("Deleting from Global Cache: " + type.getSimpleName() + " uuid " + uuid + ""); //DEBUG
            if (!globalCache().removeData(type, uuid))
                System.out.println("[GlobalCache] Could not delete: " + type.getSimpleName() + " uuid " + uuid); //DEBUG
            else
                System.out.println("[GlobalCache] Deleted: " + type.getSimpleName() + " uuid " + uuid + "" + Arrays.toString(strategies)); //DEBUG
        }
        if (globalStorage() != null && (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_STORAGE))) {
            System.out.println("Deleting from Global Storage: " + type.getSimpleName() + " uuid " + uuid + ""); //DEBUG
            if (!globalStorage().removeData(type, uuid))
                System.out.println("[GlobalStorage] Could not delete: " + type.getSimpleName() + " uuid " + uuid); //DEBUG
            else
                System.out.println("[GlobalStorage] Deleted: " + type.getSimpleName() + " uuid " + uuid + "" + Arrays.toString(strategies)); //DEBUG
        }
        return true;
    }

    @Override
    public <T extends PipelineData> CompletableFuture<Boolean> deleteAsync(@NotNull Class<? extends T> type, @NotNull UUID uuid, boolean notifyOthers, @NotNull QueryStrategy... strategies) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        executorService.submit(new CatchingRunnable(() -> completableFuture.complete(delete(type, uuid, notifyOthers, strategies))));
        return completableFuture;
    }

    private <T extends PipelineData> void synchronizeData(@NotNull Class<? extends T> type, @NotNull List<UUID> uuids) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");
        if (globalCache() != null) {
            uuids.forEach(uuid -> {
                if (globalCache.dataExist(type, uuid) && !localCache.dataExist(type, uuid))
                    pipelineDataSynchronizer.doSynchronisation(PipelineDataSynchronizer.DataSourceType.GLOBAL_CACHE, PipelineDataSynchronizer.DataSourceType.LOCAL, type, uuid, null);
            });
        }
        if (globalStorage() != null) {
            var context = AnnotationResolver.context(type);
            uuids.forEach(uuid -> {
                if (globalStorage.dataExist(type, uuid) && !localCache.dataExist(type, uuid)) {
                    pipelineDataSynchronizer.doSynchronisation(PipelineDataSynchronizer.DataSourceType.GLOBAL_STORAGE, PipelineDataSynchronizer.DataSourceType.LOCAL, type, uuid, null);

                    if (context.equals(Context.GLOBAL) && globalCache != null)
                        pipelineDataSynchronizer.doSynchronisation(PipelineDataSynchronizer.DataSourceType.GLOBAL_STORAGE, PipelineDataSynchronizer.DataSourceType.GLOBAL_CACHE, type, uuid, null);
                }
            });
        }
    }

    @Override
    public LocalCache localCache() {
        return localCache;
    }

    @Override
    public DataUpdaterService dataUpdaterService() {
        return dataUpdaterService;
    }

    @Override
    public GlobalCache globalCache() {
        return globalCache;
    }

    @Override
    public GlobalStorage globalStorage() {
        return globalStorage;
    }

    @Override
    public void saveAllData() {
        System.out.println("Saving all data...");
        registry.dataClasses().forEach(this::saveData);
    }

    @Override
    public void preloadAllData() {
        System.out.println("Preloading all data...");
        registry.dataClasses().stream().filter(aClass -> !ConnectionPipelineData.class.isAssignableFrom(aClass)).forEach(this::preloadData);
    }

    @Override
    public void preloadData(@NotNull Class<? extends PipelineData> type) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        //Connection
        if (ConnectionPipelineData.class.isAssignableFrom(type))
            return;
        var optional = AnnotationResolver.autoLoad(type);

        // Data will only be preloaded if it is declared properly
        if (!optional.isPresent()) {
            return;
        }

        var startTime = System.currentTimeMillis();
        System.out.println("Preloading " + type.getSimpleName()); //DEBUG
        if (globalCache != null)
            globalCache.savedUUIDs(type).forEach(uuid -> preloadData(type, uuid));
        if (globalStorage != null)
            globalStorage.savedUUIDs(type).forEach(uuid -> preloadData(type, uuid));
        System.out.println("Done preloading " + type.getSimpleName() + " in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    @Override
    public void preloadData(@NotNull Class<? extends PipelineData> type, @NotNull UUID uuid) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(uuid);
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");
        //Connection
        if (ConnectionPipelineData.class.isAssignableFrom(type))
            return;
        if (localCache.dataExist(type, uuid))
            return;

        pipelineDataSynchronizer.doSynchronisation(PipelineDataSynchronizer.DataSourceType.GLOBAL_CACHE, PipelineDataSynchronizer.DataSourceType.LOCAL, type, uuid, null);

        if (localCache.dataExist(type, uuid))
            return;

        pipelineDataSynchronizer.doSynchronisation(PipelineDataSynchronizer.DataSourceType.GLOBAL_STORAGE, PipelineDataSynchronizer.DataSourceType.LOCAL, type, uuid, null);
        if (AnnotationResolver.context(type).equals(Context.GLOBAL) && globalCache != null)
            pipelineDataSynchronizer.doSynchronisation(PipelineDataSynchronizer.DataSourceType.GLOBAL_STORAGE, PipelineDataSynchronizer.DataSourceType.GLOBAL_CACHE, type, uuid, null);
    }

    @Override
    public void saveData(@NotNull Class<? extends PipelineData> type) {
        Objects.requireNonNull(type, "Dataclass can't be null");
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        var optional = AnnotationResolver.autoSave(type);

        // Data will only be preloaded if it is declared properly
        if (!optional.isPresent())
            return;
        var startTime = System.currentTimeMillis();
        System.out.println("Saving " + type.getSimpleName()); //DEBUG
        localCache.savedUUIDs(type).forEach(uuid -> saveData(type, uuid, () -> {

        }));
        System.out.println("Done saving " + type.getSimpleName() + " in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    @Override
    public void saveData(@NotNull Class<? extends PipelineData> type, @NotNull UUID uuid, Runnable runnable) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(uuid);
        if (!registry.dataClasses().contains(type))
            throw new IllegalStateException("The class " + type.getSimpleName() + " is not registered in the pipeline");

        PipelineData pipelineData = localCache().data(type, uuid);
        System.out.println("Saving " + uuid + " [" + type + "]");
        if (pipelineData == null)
            return;
        if (pipelineData.isMarkedForRemoval())
            return;
        pipelineData.onCleanUp();

        var optional = AnnotationResolver.autoSave(type);

        if (!optional.isPresent())
            return;

        pipelineData.save(optional.get().saveToGlobalStorage(), () -> {
            localCache().remove(type, pipelineData.objectUUID());
            if (runnable != null)
                runnable.run();
        });

    }

    @Override
    public PipelineDataSynchronizer dataSynchronizer() {
        return pipelineDataSynchronizer;
    }

    private <T extends PipelineData> T loadFromPipeline(@NotNull Class<? extends T> dataClass, @NotNull UUID uuid, @NotNull QueryStrategy... creationStrategies) {
        Objects.requireNonNull(dataClass, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.dataClasses().contains(dataClass))
            throw new IllegalStateException("The class " + dataClass.getSimpleName() + " is not registered in the pipeline");
        var startTime = System.currentTimeMillis();
        // ExistCheck LocalCache
        if (localCache.dataExist(dataClass, uuid)) {
            System.out.println("Found Data in Local Cache [" + dataClass.getSimpleName() + "]");
        }
        // ExistCheck GlobalCache
        else if (globalCache != null && globalCache.dataExist(dataClass, uuid)) {
            System.out.println("Found Data in Redis Cache [" + dataClass.getSimpleName() + "]"); //DEBUG
            pipelineDataSynchronizer.doSynchronisation(PipelineDataSynchronizer.DataSourceType.GLOBAL_CACHE, PipelineDataSynchronizer.DataSourceType.LOCAL, dataClass, uuid, null);
        }
        // ExistCheck GlobalStorage
        else if (globalStorage != null && globalStorage.dataExist(dataClass, uuid)) {
            System.out.println("Found Data in Database [" + dataClass.getSimpleName() + "]"); //DEBUG
            pipelineDataSynchronizer.doSynchronisation(PipelineDataSynchronizer.DataSourceType.GLOBAL_STORAGE, PipelineDataSynchronizer.DataSourceType.LOCAL, dataClass, uuid, null);

            if (AnnotationResolver.context(dataClass).equals(Context.GLOBAL) && globalCache != null)
                pipelineDataSynchronizer.doSynchronisation(PipelineDataSynchronizer.DataSourceType.GLOBAL_STORAGE, PipelineDataSynchronizer.DataSourceType.GLOBAL_CACHE, dataClass, uuid, null);
        } else {
            System.out.println(creationStrategies.length);
            if (creationStrategies.length <= 0)
                return null;
            T data = createNewData(dataClass, uuid, creationStrategies);
            data.updateLastUse();
            System.out.println("Done loading in " + (System.currentTimeMillis() - startTime) + "ms");
            return data;
        }
        System.out.println("Done loading in " + (System.currentTimeMillis() - startTime) + "ms");
        if (!localCache.dataExist(dataClass, uuid)) {
            System.out.println("Data deleted from other thread while loading " + dataClass.getSimpleName() + " with uuid: " + uuid); //DEBUG
            return null;
        }
        T data = localCache.data(dataClass, uuid);
        if (data != null)
            data.updateLastUse();

        return data;
    }

    private <T extends PipelineData> T createNewData(@NotNull Class<? extends T> dataClass, @NotNull UUID uuid, @NotNull QueryStrategy... queryStrategies) {
        Objects.requireNonNull(dataClass, "Dataclass can't be null");
        Objects.requireNonNull(uuid, "UUID can't be null");
        if (!registry.dataClasses().contains(dataClass))
            throw new IllegalStateException("The class " + dataClass.getSimpleName() + " is not registered in the pipeline");

        var strategySet = Arrays.stream(queryStrategies).collect(Collectors.toSet());
        System.out.println("No Data was found. Creating new data! [" + dataClass.getSimpleName() + "]"); //DEBUG
        T pipelineData = localCache.data(dataClass, uuid);

        if (pipelineData == null) {
            if (queryStrategies.length > 0) {
                pipelineData = localCache.instantiateData(this, dataClass, uuid);

                pipelineData.loadDependentData();
                pipelineData.onCreate();

                if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.LOCAL)) {
                    localCache.save(dataClass, pipelineData);
                }
                if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_CACHE)) {
                    pipelineDataSynchronizer.synchronize(PipelineDataSynchronizer.DataSourceType.LOCAL, PipelineDataSynchronizer.DataSourceType.GLOBAL_CACHE, dataClass, uuid);
                }
                if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_STORAGE)) {
                    pipelineDataSynchronizer.synchronize(PipelineDataSynchronizer.DataSourceType.LOCAL, PipelineDataSynchronizer.DataSourceType.GLOBAL_STORAGE, dataClass, uuid);
                }
            }
        } else {
            pipelineData.loadDependentData();

            localCache.save(dataClass, pipelineData);
            pipelineDataSynchronizer.synchronize(PipelineDataSynchronizer.DataSourceType.LOCAL, PipelineDataSynchronizer.DataSourceType.GLOBAL_CACHE, dataClass, uuid);
            pipelineDataSynchronizer.synchronize(PipelineDataSynchronizer.DataSourceType.LOCAL, PipelineDataSynchronizer.DataSourceType.GLOBAL_STORAGE, dataClass, uuid);
        }

        return pipelineData;
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

    public ExecutorService executorService() {
        return executorService;
    }

    @Override
    public PipelineRegistry registry() {
        return registry;
    }
}
