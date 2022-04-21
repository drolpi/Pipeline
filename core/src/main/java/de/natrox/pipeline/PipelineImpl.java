/*
 * Copyright 2020-2022 NatroxMC team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.natrox.pipeline;

import com.google.common.base.Preconditions;
import de.natrox.common.runnable.CatchingRunnable;
import de.natrox.common.scheduler.Scheduler;
import de.natrox.pipeline.annotation.property.Context;
import de.natrox.pipeline.annotation.resolver.AnnotationResolver;
import de.natrox.pipeline.cleanup.CleanUpTask;
import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.connection.ConnectionData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import de.natrox.pipeline.json.JsonProvider;
import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.json.serializer.PipelineDataSerializer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class PipelineImpl implements Pipeline {

    private final static Logger LOGGER = LoggerFactory.getLogger(PipelineImpl.class);

    private final GlobalStorage globalStorage;
    private final GlobalCache globalCache;
    private final DataUpdater dataUpdater;
    private final LocalCache localCache;
    private final PipelineRegistry registry;
    private final DataSynchronizerImpl dataSynchronizer;
    private final PipelineTaskScheduler pipelineTaskScheduler;
    private final ExecutorService executorService;
    private final Scheduler scheduler;
    private final JsonProvider jsonProvider;
    private final boolean loaded;

    protected PipelineImpl(
        @Nullable DataUpdaterProvider dataUpdaterProvider,
        @Nullable GlobalCacheProvider globalCacheProvider,
        @Nullable GlobalStorageProvider globalStorageProvider,
        @Nullable JsonProvider jsonProvider,
        @Nullable PipelineRegistry registry
    ) {
        Preconditions.checkNotNull(jsonProvider, "jsonProvider");
        Preconditions.checkNotNull(registry, "registry");

        this.jsonProvider = jsonProvider;
        this.registry = registry;
        this.executorService = Executors.newFixedThreadPool(4);
        this.localCache = new DefaultLocalCache();

        LOGGER.debug("Pipeline information:");
        LOGGER.debug("Local cache: " + localCache.getClass().getName());

        // check if there is a data updater provider or initialize the default one
        if (dataUpdaterProvider != null) {
            this.dataUpdater = dataUpdaterProvider.constructDataUpdater(this);
        } else {
            this.dataUpdater = new DefaultDataUpdater();
        }

        LOGGER.debug("Data updater: " + dataUpdater.getClass().getName());

        // check if there is a global cache provider or initialize null
        if (globalCacheProvider != null) {
            this.globalCache = globalCacheProvider.constructGlobalCache(this);
            LOGGER.debug("GlobalCache: " + globalCache.getClass().getName());
        } else {
            this.globalCache = null;
            LOGGER.debug("No global cache found");
        }

        // check if there is a global storage provider or or initialize null
        if (globalStorageProvider != null) {
            this.globalStorage = globalStorageProvider.constructGlobalStorage(this);
            LOGGER.debug("Global storage: " + globalStorage.getClass().getName());
        } else {
            this.globalStorage = null;
            LOGGER.debug("No global storage found");
        }

        LOGGER.debug("JsonProvider: " + jsonProvider.getClass().getName());

        this.scheduler = Scheduler.create();
        this.pipelineTaskScheduler = new PipelineTaskSchedulerImpl();
        this.dataSynchronizer = new DataSynchronizerImpl(this);
        this.scheduler
            .buildTask(new CleanUpTask(this))
            .repeat(300, TimeUnit.SECONDS)
            .schedule();
        loaded = true;
    }

    @Override
    public @NotNull <T extends PipelineData> PipelineStream<T> find(
        @NotNull Class<? extends T> dataClass,
        @Nullable Consumer<T> callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        return new PipelineStreamImpl<>(this, dataSynchronizer, dataClass, callback, instanceCreator);
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

        if (localCache.exists(dataClass, objectUUID)) {
            T data = localCache.get(dataClass, objectUUID);
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
                return Optional.of(data);
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

        return localCache.keys(dataClass)
            .stream()
            .map(uuid -> localCache.get(dataClass, uuid))
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
            var localExist = localCache().exists(dataClass, objectUUID);
            if (localExist)
                return true;
        }
        if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_CACHE)) {
            if (globalCache != null) {
                var globalCacheExists = globalCache.exists(dataClass, objectUUID);
                if (globalCacheExists)
                    return true;
            }
        }
        if (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_STORAGE)) {
            if (globalStorage != null)
                return globalStorage.exists(dataClass, objectUUID);
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
            T data = localCache().get(dataClass, objectUUID);

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
            if (!globalCache.remove(dataClass, objectUUID))
                LOGGER.debug("[GlobalCache] Could not delete: " + dataClass.getSimpleName() + " uuid " + objectUUID);
            else
                LOGGER.debug("[GlobalCache] Deleted: " + dataClass.getSimpleName() + " uuid " + objectUUID + "" + Arrays.toString(strategies));
        }
        if (globalStorage != null && (strategySet.contains(QueryStrategy.ALL) || strategySet.contains(QueryStrategy.GLOBAL_STORAGE))) {
            LOGGER.debug("Deleting from Global Storage: " + dataClass.getSimpleName() + " uuid " + objectUUID + "");
            if (!globalStorage.remove(dataClass, objectUUID))
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

    @Override
    public @NotNull Collection<UUID> keys(@NotNull Class<? extends PipelineData> dataClass) {
        if (globalStorage != null) {
            return globalStorage.keys(dataClass);
        } else if (globalCache != null) {
            return globalCache.keys(dataClass);
        } else {
            return localCache.keys(dataClass);
        }
    }

    @Override
    public @NotNull Collection<JsonDocument> documents(@NotNull Class<? extends PipelineData> dataClass) {
        if (globalStorage != null) {
            return globalStorage.documents(dataClass);
        } else if (globalCache != null) {
            return globalCache.documents(dataClass);
        } else {
            return localCache.values(dataClass).stream().map(PipelineData::serialize).collect(Collectors.toList());
        }
    }

    @Override
    public @NotNull Map<UUID, JsonDocument> entries(@NotNull Class<? extends PipelineData> dataClass) {
        if (globalStorage != null) {
            return globalStorage.entries(dataClass);
        } else if (globalCache != null) {
            return globalCache.entries(dataClass);
        } else {
            Map<UUID, JsonDocument> entries = new HashMap<>();
            for (var entry : localCache.entries(dataClass).entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue().serialize();

                entries.put(key, value);
            }
            return entries;
        }
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
            var startInstant = Instant.now();
            LOGGER.debug("Preloading " + dataClass.getSimpleName());
            if (globalCache != null)
                globalCache.keys(dataClass).forEach(uuid -> preloadData(dataClass, uuid));
            if (globalStorage != null)
                globalStorage.keys(dataClass).forEach(uuid -> preloadData(dataClass, uuid));
            LOGGER.debug("Done preloading " + dataClass.getSimpleName() + " in " + Duration.between(startInstant, Instant.now()).toMillis() + "ms");
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
        if (localCache.exists(dataClass, objectUUID))
            return;

        dataSynchronizer.doSynchronisation(
            DataSynchronizer.DataSourceType.GLOBAL_CACHE, DataSynchronizer.DataSourceType.LOCAL, dataClass, objectUUID, null, null
        );

        if (localCache.exists(dataClass, objectUUID))
            return;

        dataSynchronizer.doSynchronisation(
            DataSynchronizer.DataSourceType.GLOBAL_STORAGE, DataSynchronizer.DataSourceType.LOCAL, dataClass, objectUUID, null, null
        );

        if (AnnotationResolver.context(dataClass).equals(Context.GLOBAL) && globalCache != null)
            dataSynchronizer.doSynchronisation(
                DataSynchronizer.DataSourceType.GLOBAL_STORAGE, DataSynchronizer.DataSourceType.GLOBAL_CACHE, dataClass, objectUUID, null, null
            );
    }

    @Override
    public void cleanUpData(@NotNull Class<? extends PipelineData> dataClass) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        registry.checkRegistered(dataClass);

        var optional = AnnotationResolver.autoSave(dataClass);

        // Data will only be cleaned up if it is declared properly
        optional.ifPresent(unload -> {
            var startInstant = Instant.now();
            LOGGER.debug("Saving " + dataClass.getSimpleName());
            localCache.keys(dataClass).forEach(uuid -> cleanUpData(dataClass, uuid, null));
            LOGGER.debug("Done saving " + dataClass.getSimpleName() + " in " + Duration.between(startInstant, Instant.now()) + "ms");
        });
    }

    @Override
    public void cleanUpData(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, Runnable runnable) {
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
        registry.checkRegistered(dataClass);

        var optional = AnnotationResolver.autoSave(dataClass);

        optional.ifPresent(autoSave -> {
            var pipelineData = localCache().get(dataClass, objectUUID);
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
        return dataSynchronizer;
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

        var startInstant = Instant.now();
        // ExistCheck LocalCache
        if (localCache.exists(dataClass, objectUUID))
            LOGGER.debug("Found Data in Local Cache [" + dataClass.getSimpleName() + "]");
            // ExistCheck GlobalCache
        else {
            boolean globalCacheExists = dataSynchronizer.doSynchronisation(
                DataSynchronizer.DataSourceType.GLOBAL_CACHE, DataSynchronizer.DataSourceType.LOCAL, dataClass, objectUUID, null, instanceCreator
            );

            if (globalCacheExists)
                LOGGER.debug("Found Data in Redis Cache [" + dataClass.getSimpleName() + "]");
            else {
                boolean globalStorageExists = dataSynchronizer.doSynchronisation(
                    DataSynchronizer.DataSourceType.GLOBAL_STORAGE, DataSynchronizer.DataSourceType.LOCAL, dataClass, objectUUID, null, instanceCreator
                );

                if (globalStorageExists) {
                    LOGGER.debug("Found Data in Database [" + dataClass.getSimpleName() + "]");
                    if (AnnotationResolver.context(dataClass).equals(Context.GLOBAL))
                        dataSynchronizer.synchronize(
                            DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_CACHE, dataClass, objectUUID, null, instanceCreator
                        );
                } else {
                    if (!createIfNotExists)
                        return null;
                    T data = createNewData(dataClass, objectUUID, instanceCreator);
                    data.updateLastUse();
                    LOGGER.debug("Done loading in " + Duration.between(startInstant, Instant.now()).toMillis() + "ms");
                    return data;
                }
            }
        }
        LOGGER.debug("Done loading in " + Duration.between(startInstant, Instant.now()) + "ms");
        T data = localCache.get(dataClass, objectUUID);
        if (data != null)
            data.updateLastUse();
        else
            LOGGER.warn("Data deleted from other thread while loading " + dataClass.getSimpleName() + " with uuid: " + objectUUID);

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
        T pipelineData = localCache.get(dataClass, objectUUID);

        if (pipelineData == null) {
            pipelineData = localCache.instantiateData(this, dataClass, objectUUID, instanceCreator);
            pipelineData.onCreate();
        }
        pipelineData.loadDependentData();

        localCache.save(dataClass, pipelineData);
        dataSynchronizer.synchronize(
            DataSynchronizer.DataSourceType.LOCAL, DataSynchronizer.DataSourceType.GLOBAL_CACHE, dataClass, objectUUID, null, instanceCreator
        );
        dataSynchronizer.synchronize(
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
                if (!localCache.exists(dataClass, uuid))
                    dataSynchronizer.doSynchronisation(
                        DataSynchronizer.DataSourceType.GLOBAL_CACHE, DataSynchronizer.DataSourceType.LOCAL, dataClass, uuid, null, instanceCreator
                    );
            });
        }
        if (globalStorage() != null) {
            var context = AnnotationResolver.context(dataClass);
            objectUUIDs.forEach(uuid -> {
                if (!localCache.exists(dataClass, uuid)) {
                    dataSynchronizer.doSynchronisation(
                        DataSynchronizer.DataSourceType.GLOBAL_STORAGE, DataSynchronizer.DataSourceType.LOCAL, dataClass, uuid, null, instanceCreator
                    );

                    if (context.equals(Context.GLOBAL) && globalCache != null)
                        dataSynchronizer.synchronize(
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
            dataSynchronizer.shutdown();
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
    public JsonDocument.Factory documentFactory() {
        return jsonProvider.documentFactory();
    }

    @Override
    public @NotNull PipelineDataSerializer.Factory serializerFactory() {
        return jsonProvider.serializerFactory();
    }
}
