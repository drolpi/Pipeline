/*
 * Copyright 2020-2022 NatroxMC
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

package de.natrox.pipeline.repository;

import de.natrox.common.runnable.CatchingRunnable;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.store.StoreMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class DataSynchronizer {

    private final StoreMap storage;
    private final @Nullable StoreMap globalCache;
    private final @Nullable StoreMap localCache;
    private final ExecutorService executorService;

    private volatile boolean closed;

    DataSynchronizer(StoreMap storageMap, @Nullable StoreMap globalCacheMap, @Nullable StoreMap localCacheMap) {
        this.storage = storageMap;
        this.globalCache = globalCacheMap;
        this.localCache = localCacheMap;
        this.executorService = Executors.newCachedThreadPool();
        this.closed = false;
    }

    public CompletableFuture<Boolean> synchronizeTo(@NotNull UUID uniqueId, @NotNull Object data, DataSourceType @NotNull ... destinations) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(data, "data");
        Check.notNull(destinations, "destinations");
        if (this.isOpen())
            return CompletableFuture.completedFuture(false);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        this.executorService.submit(new CatchingRunnable(() -> future.complete(this.to(uniqueId, data, destinations))));
        return future;
    }

    private boolean to(@NotNull UUID uniqueId, @NotNull Object data, DataSourceType @NotNull ... destinations) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(data, "data");
        Check.notNull(destinations, "destinations");
        if (this.isOpen())
            return false;

        List<DataSourceType> destinationList = Arrays.asList(destinations);
        if (this.localCache != null && destinationList.contains(DataSourceType.LOCAL_CACHE)) {
            this.localCache.put(uniqueId, data);
        }
        if (this.globalCache != null && destinationList.contains(DataSourceType.GLOBAL_CACHE)) {
            this.globalCache.put(uniqueId, data);
        }
        if (destinationList.contains(DataSourceType.STORAGE)) {
            this.storage.put(uniqueId, data);
        }
        return true;
    }

    public CompletableFuture<Object> synchronizeFom(@NotNull UUID uniqueId, @NotNull DataSourceType source) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(source, "source");
        if (this.isOpen())
            return CompletableFuture.completedFuture(null);

        CompletableFuture<Object> future = new CompletableFuture<>();
        this.executorService.submit(new CatchingRunnable(() -> future.complete(this.from(uniqueId, source))));
        return future;
    }

    public Object from(@NotNull UUID uniqueId, @NotNull DataSourceType source) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(source, "source");
        if (this.isOpen())
            return null;

        if (this.localCache != null && source.equals(DataSourceType.LOCAL_CACHE)) {
            return this.localCache.get(uniqueId);
        } else if (this.globalCache != null && source.equals(DataSourceType.GLOBAL_CACHE)) {
            return this.globalCache.get(uniqueId);
        } else if (source.equals(DataSourceType.STORAGE)) {
            return this.storage.get(uniqueId);
        }
        return null;
    }

    public CompletableFuture<Boolean> synchronizeFromTo(@NotNull UUID uniqueId, @NotNull DataSourceType source, DataSourceType @NotNull ... destinations) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(source, "source");
        Check.notNull(destinations, "destinations");
        if (this.isOpen())
            return CompletableFuture.completedFuture(false);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        this.executorService.submit(new CatchingRunnable(() -> future.complete(this.fromTo(uniqueId, source, destinations))));
        return future;
    }

    public boolean fromTo(@NotNull UUID uniqueId, @NotNull DataSourceType source, DataSourceType @NotNull ... destinations) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(source, "source");
        Check.notNull(destinations, "destinations");
        if (this.isOpen())
            return false;

        Object documentData = this.from(uniqueId, source);
        // Error while loading from local cache
        if (documentData == null)
            return false;
        this.to(uniqueId, documentData, destinations);
        return true;
    }

    public boolean isOpen() {
        return !this.closed;
    }

    public void close() {
        this.closed = true;
        this.executorService.shutdown();
    }

    public enum DataSourceType {
        LOCAL_CACHE,
        GLOBAL_CACHE,
        STORAGE
    }
}
