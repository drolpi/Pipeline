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

import de.natrox.common.container.Pair;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.part.LocalUpdater;
import de.natrox.pipeline.part.StoreMap;
import de.natrox.pipeline.stream.PipeStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ConnectingMap implements StoreMap {

    private final StoreMap storageMap;
    private final @Nullable StoreMap globalCacheMap;
    private final @Nullable StoreMap localCacheMap;
    private final @Nullable LocalUpdater localUpdater;

    private final DataSynchronizer dataSynchronizer;

    public ConnectingMap(StoreMap storageMap, @Nullable StoreMap globalCacheMap, @Nullable StoreMap localCacheMap, @Nullable LocalUpdater localUpdater) {
        this.storageMap = storageMap;
        this.globalCacheMap = globalCacheMap;
        this.localCacheMap = localCacheMap;
        this.localUpdater = localUpdater;
        this.dataSynchronizer = new DataSynchronizer(this);
    }

    @Override
    public @Nullable DocumentData get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");

        if (this.localCacheMap != null) {
            DocumentData documentData = this.fromPart(uniqueId, this.localCacheMap);
            if (documentData != null) {
                return documentData;
            }
        }

        if (this.globalCacheMap != null) {
            DocumentData documentData = this.fromPart(uniqueId, this.globalCacheMap, DataSynchronizer.DataSourceType.LOCAL_CACHE);
            if (documentData != null) {
                return documentData;
            }
        }

        return this.fromPart(uniqueId, this.storageMap, DataSynchronizer.DataSourceType.LOCAL_CACHE, DataSynchronizer.DataSourceType.GLOBAL_CACHE);
    }

    private DocumentData fromPart(UUID uniqueId, StoreMap storeMap, DataSynchronizer.DataSourceType... destinations) {
        DocumentData documentData = storeMap.get(uniqueId);
        this.dataSynchronizer.synchronizeTo(uniqueId, documentData, destinations);
        return documentData;
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull DocumentData documentData) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(documentData, "documentData");
        if (this.localCacheMap != null && this.localUpdater != null) {
            this.localCacheMap.put(uniqueId, documentData);
            this.localUpdater.pushUpdate(uniqueId, documentData, () -> {

            });
        }
        if (this.globalCacheMap != null) {
            this.globalCacheMap.put(uniqueId, documentData);
        }
        this.storageMap.put(uniqueId, documentData);
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        if (this.localCacheMap != null) {
            boolean localExists = this.localCacheMap.contains(uniqueId);
            if (localExists)
                return true;
        }

        if (this.globalCacheMap != null) {
            boolean globalExists = this.globalCacheMap.contains(uniqueId);
            if (globalExists)
                return true;
        }

        return this.storageMap.contains(uniqueId);
    }

    @Override
    public @NotNull PipeStream<UUID> keys() {
        return this.storageMap.keys();
    }

    @Override
    public @NotNull PipeStream<DocumentData> values() {
        return this.storageMap.values();
    }

    @Override
    public @NotNull PipeStream<Pair<UUID, DocumentData>> entries() {
        return this.storageMap.entries();
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        if (this.localCacheMap != null && this.localUpdater != null) {
            this.localCacheMap.remove(uniqueId);
            this.localUpdater.pushRemoval(uniqueId, () -> {

            });
        }
        if (this.globalCacheMap != null) {
            this.globalCacheMap.remove(uniqueId);
        }
        this.storageMap.remove(uniqueId);
    }

    @Override
    public void clear() {
        if (this.localCacheMap != null && this.localUpdater != null) {
            this.localCacheMap.clear();
            this.localUpdater.pushClear(() -> {

            });
        }
        if (this.globalCacheMap != null) {
            this.globalCacheMap.clear();
        }
        this.storageMap.clear();
    }

    @Override
    public long size() {
        return this.storageMap.size();
    }

    public StoreMap storageMap() {
        return this.storageMap;
    }

    public StoreMap globalCacheMap() {
        return this.globalCacheMap;
    }

    public StoreMap localCacheMap() {
        return this.localCacheMap;
    }
}
