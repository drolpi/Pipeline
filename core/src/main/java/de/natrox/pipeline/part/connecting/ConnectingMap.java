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
import de.natrox.pipeline.document.PipeDocument;
import de.natrox.pipeline.part.PartMap;
import de.natrox.pipeline.part.cache.DataUpdater;
import de.natrox.pipeline.stream.PipeStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ConnectingMap implements PartMap {

    private final PartMap storageMap;
    private final @Nullable PartMap globalCacheMap;
    private final @Nullable PartMap localCacheMap;
    private final @Nullable DataUpdater dataUpdater;

    private final DataSynchronizer dataSynchronizer;

    public ConnectingMap(PartMap storageMap, @Nullable PartMap globalCacheMap, @Nullable PartMap localCacheMap, @Nullable DataUpdater dataUpdater) {
        this.storageMap = storageMap;
        this.globalCacheMap = globalCacheMap;
        this.localCacheMap = localCacheMap;
        this.dataUpdater = dataUpdater;
        this.dataSynchronizer = new DataSynchronizer(this);
    }

    @Override
    public @Nullable PipeDocument get(@NotNull UUID uniqueId) {
        if (this.localCacheMap != null) {
            PipeDocument document = this.getFromPart(uniqueId, this.localCacheMap);
            if (document != null) {
                return document;
            }
        }

        if (this.globalCacheMap != null) {
            PipeDocument document = this.getFromPart(uniqueId, this.globalCacheMap, DataSynchronizer.DataSourceType.LOCAL_CACHE);
            if (document != null) {
                return document;
            }
        }

        return this.getFromPart(uniqueId, this.storageMap, DataSynchronizer.DataSourceType.LOCAL_CACHE, DataSynchronizer.DataSourceType.GLOBAL_CACHE);
    }

    private PipeDocument getFromPart(UUID uniqueId, PartMap partMap, DataSynchronizer.DataSourceType... destinations) {
        PipeDocument document = partMap.get(uniqueId);
        this.dataSynchronizer.synchronizeTo(uniqueId, document, destinations);
        return document;
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull PipeDocument document) {
        if (this.localCacheMap != null) {
            this.localCacheMap.put(uniqueId, document);
        }
        //TODO: Push data updater
        if (this.globalCacheMap != null) {
            this.globalCacheMap.put(uniqueId, document);
        }
        this.storageMap.put(uniqueId, document);
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
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
    public @NotNull PipeStream<PipeDocument> values() {
        return this.storageMap.values();
    }

    @Override
    public @NotNull PipeStream<Pair<UUID, PipeDocument>> entries() {
        return this.storageMap.entries();
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        if (this.localCacheMap != null) {
            this.localCacheMap.remove(uniqueId);
        }
        //TODO: Push data Updater
        if (this.globalCacheMap != null) {
            this.globalCacheMap.remove(uniqueId);
        }
        this.storageMap.remove(uniqueId);
    }

    @Override
    public void clear() {

    }

    @Override
    public long size() {
        return this.storageMap.size();
    }

    public PartMap storageMap() {
        return this.storageMap;
    }

    public PartMap globalCacheMap() {
        return this.globalCacheMap;
    }

    public PartMap localCacheMap() {
        return this.localCacheMap;
    }
}
