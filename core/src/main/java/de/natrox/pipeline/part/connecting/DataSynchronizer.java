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

package de.natrox.pipeline.part.connecting;

import de.natrox.common.runnable.CatchingRunnable;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.document.PipeDocument;
import de.natrox.pipeline.part.PartMap;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class DataSynchronizer {

    private final PartMap storage;
    private final @Nullable PartMap globalCache;
    private final @Nullable PartMap localCache;
    private final ExecutorService executorService;

    public DataSynchronizer(ConnectingMap connectingMap) {
        this.storage = connectingMap.storageMap();
        this.globalCache = connectingMap.globalCacheMap();
        this.localCache = connectingMap.localCacheMap();
        this.executorService = Executors.newCachedThreadPool();
    }

    public void synchronizeTo(UUID uniqueId, PipeDocument document, DataSourceType... destinations) {
        this.executorService.submit(new CatchingRunnable(() -> this.to(uniqueId, document, destinations)));
    }

    public boolean to(UUID uniqueId, PipeDocument document, DataSourceType... destinations) {
        List<DataSourceType> destinationList = Arrays.asList(destinations);
        if (this.localCache != null && destinationList.contains(DataSourceType.LOCAL_CACHE)) {
            this.localCache.put(uniqueId, document);
        }
        if (this.globalCache != null && destinationList.contains(DataSourceType.GLOBAL_CACHE)) {
            this.globalCache.put(uniqueId, document);
        }
        if (destinationList.contains(DataSourceType.STORAGE)) {
            this.storage.put(uniqueId, document);
        }
        return true;
    }

    public CompletableFuture<PipeDocument> synchronizeFom(UUID uniqueId, DataSourceType source) {
        CompletableFuture<PipeDocument> future = new CompletableFuture<>();
        this.executorService.submit(new CatchingRunnable(() -> future.complete(this.from(uniqueId, source))));
        return future;
    }

    public PipeDocument from(UUID uniqueId, DataSourceType source) {
        if (this.localCache != null && source.equals(DataSourceType.LOCAL_CACHE)) {
            return this.localCache.get(uniqueId);
        } else if (this.globalCache != null && source.equals(DataSourceType.GLOBAL_CACHE)) {
            return this.globalCache.get(uniqueId);
        } else if (source.equals(DataSourceType.STORAGE)) {
            return this.storage.get(uniqueId);
        }
        return null;
    }

    public void synchronizeFromTo(UUID uniqueId, DataSourceType sourceType, DataSourceType... destinations) {
        this.executorService.submit(new CatchingRunnable(() -> this.fromTo(uniqueId, sourceType, destinations)));
    }

    public boolean fromTo(UUID uniqueId, DataSourceType source, DataSourceType... destinations) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(source, "source");
        Check.notNull(destinations, "destinations");
        PipeDocument document = from(uniqueId, source);
        // Error while loading from local cache
        if (document == null)
            return false;
        to(uniqueId, document, destinations);
        return true;
    }

    public enum DataSourceType {
        LOCAL_CACHE,
        GLOBAL_CACHE,
        STORAGE
    }

}
