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

package de.natrox.pipeline.part;

import com.google.common.base.Preconditions;
import de.natrox.common.runnable.CatchingRunnable;
import de.natrox.pipeline.PipelineImpl;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.local.LocalCache;
import de.natrox.pipeline.part.storage.GlobalStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class DataSynchronizerImpl implements DataSynchronizer {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataSynchronizerImpl.class);

    private final PipelineImpl pipelineImpl;
    private final LocalCache localCache;
    private final GlobalCache globalCache;
    private final GlobalStorage globalStorage;
    private final ExecutorService executorService;

    public DataSynchronizerImpl(@NotNull PipelineImpl pipelineImpl) {
        this.pipelineImpl = pipelineImpl;
        this.localCache = pipelineImpl.localCache();
        this.globalCache = pipelineImpl.globalCache();
        this.globalStorage = pipelineImpl.globalStorage();
        this.executorService = pipelineImpl.executorService();
    }

    @Override
    public <T extends PipelineData> @NotNull CompletableFuture<Boolean> synchronize(
        @NotNull DataSourceType source,
        @NotNull DataSourceType destination,
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID objectUUID,
        @Nullable Runnable callback,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        Preconditions.checkNotNull(source, "source");
        Preconditions.checkNotNull(destination, "destination");
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");
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
        Preconditions.checkNotNull(source, "source");
        Preconditions.checkNotNull(destination, "destination");
        Preconditions.checkNotNull(dataClass, "dataClass");
        Preconditions.checkNotNull(objectUUID, "objectUUID");

        if (source.equals(destination))
            return false;
        if (globalCache == null && (source.equals(DataSourceType.GLOBAL_CACHE) || destination.equals(DataSourceType.GLOBAL_CACHE)))
            return false;
        if (globalStorage == null && (source.equals(DataSourceType.GLOBAL_STORAGE) || destination.equals(DataSourceType.GLOBAL_STORAGE)))
            return false;

        var startInstant = Instant.now();

        if (source.equals(DataSourceType.LOCAL)) {
            var data = localCache.get(dataClass, objectUUID);
            if (data == null)
                return false;
            data.updateLastUse();
            data.unMarkRemoval();
            var dataToSave = data.serialize();
            LOGGER.debug("Syncing " + dataClass.getSimpleName() + " with uuid " + objectUUID + " [" + DataSourceType.LOCAL + " -> " + destination + "]");
            if (destination.equals(DataSourceType.GLOBAL_CACHE))
                // Local to Global Cache
                globalCache.save(dataClass, objectUUID, dataToSave);
            else if (destination.equals(DataSourceType.GLOBAL_STORAGE))
                // Local to Global Storage
                globalStorage.save(dataClass, objectUUID, dataToSave);
        } else if (source.equals(DataSourceType.GLOBAL_CACHE)) {
            var globalCachedData = globalCache.get(dataClass, objectUUID);
            // Error while loading from global cache
            if (globalCachedData == null)
                return false;
            LOGGER.debug("Syncing " + dataClass.getSimpleName() + " with uuid " + objectUUID + " [" + DataSourceType.GLOBAL_CACHE + " -> " + destination + "]");
            if (destination.equals(DataSourceType.LOCAL)) {
                var localData = toLocal(dataClass, objectUUID, globalCachedData, instanceCreator);
                // Error while storing in local cache
                if (localData == null)
                    return false;
            } else if (destination.equals(DataSourceType.GLOBAL_STORAGE))
                globalStorage.save(dataClass, objectUUID, globalCachedData);

        } else if (source.equals(DataSourceType.GLOBAL_STORAGE)) {
            var globalSavedData = globalStorage.get(dataClass, objectUUID);
            // Error while loading from global storage
            if (globalSavedData == null)
                return false;
            LOGGER.debug("Syncing " + dataClass.getSimpleName() + " with uuid " + objectUUID + " [" + DataSourceType.GLOBAL_STORAGE + " -> " + destination + "]");
            if (destination.equals(DataSourceType.LOCAL)) {
                var localData = toLocal(dataClass, objectUUID, globalSavedData, instanceCreator);
                // Error while storing in local cache
                if (localData == null)
                    return false;
            } else if (destination.equals(DataSourceType.GLOBAL_CACHE))
                globalCache.save(dataClass, objectUUID, globalSavedData);
        }

        if (destination.equals(DataSourceType.LOCAL)) {
            var dataUpdater = pipelineImpl.dataUpdater();
            var data = localCache.get(dataClass, objectUUID);
            var optional = dataUpdater.applySync(data);
            optional.ifPresent(pipelineData -> localCache.save(dataClass, pipelineData));
        }

        if (callback != null)
            callback.run();
        LOGGER.debug("Done syncing in " + Duration.between(startInstant, Instant.now()) + "ms [" + dataClass.getSimpleName() + "]");
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

    public <T extends PipelineData> @Nullable T toLocal(
        @NotNull Class<? extends T> dataClass,
        @NotNull UUID objectUUID,
        @NotNull JsonDocument document,
        @Nullable InstanceCreator<T> instanceCreator
    ) {
        if (!localCache.exists(dataClass, objectUUID))
            localCache.save(
                dataClass,
                localCache.instantiateData(pipelineImpl, dataClass, objectUUID, instanceCreator)
            );

        var data = localCache.get(dataClass, objectUUID);
        if (data == null)
            return null;
        data.deserialize(document);
        data.updateLastUse();
        data.loadDependentData();
        data.onLoad();
        localCache.save(dataClass, data);
        return data;
    }
}
