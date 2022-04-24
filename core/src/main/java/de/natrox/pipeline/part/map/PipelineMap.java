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

package de.natrox.pipeline.part.map;

import de.natrox.common.container.Pair;
import de.natrox.pipeline.document.Document;
import de.natrox.pipeline.part.DataSynchronizer;
import de.natrox.pipeline.part.cache.DataUpdater;
import de.natrox.pipeline.stream.PipelineStream;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class PipelineMap implements PartMap {

    private final PartMap storageMap;
    private final PartMap globalCacheMap;
    private final PartMap localCacheMap;

    private final DataUpdater dataUpdater;
    private final DataSynchronizer dataSynchronizer;

    public PipelineMap(PartMap storageMap, PartMap globalCacheMap, PartMap localCacheMap, DataUpdater dataUpdater) {
        this.storageMap = storageMap;
        this.globalCacheMap = globalCacheMap;
        this.localCacheMap = localCacheMap;
        this.dataUpdater = dataUpdater;
        this.dataSynchronizer = new DataSynchronizer();
    }

    @Override
    public Document get(@NotNull UUID uniqueId) {
        if (localCacheMap.contains(uniqueId)) {
            return localCacheMap.get(uniqueId);
        } else if (globalCacheMap.contains(uniqueId)) {
            dataSynchronizer.fromTo(
                uniqueId,
                DataSynchronizer.DataSourceType.GLOBAL_CACHE,
                DataSynchronizer.DataSourceType.LOCAL_CACHE
            );
            return globalCacheMap.get(uniqueId);
        }

        dataSynchronizer.fromTo(
            uniqueId,
            DataSynchronizer.DataSourceType.GLOBAL_STORAGE,
            DataSynchronizer.DataSourceType.GLOBAL_CACHE,
            DataSynchronizer.DataSourceType.LOCAL_CACHE
        );
        return storageMap.get(uniqueId);
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull Document document) {
        localCacheMap.put(uniqueId, document);
        //TODO: Push data updater
        globalCacheMap.put(uniqueId, document);
        storageMap.put(uniqueId, document);
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        boolean localExists = localCacheMap.contains(uniqueId);
        if (localExists)
            return true;
        boolean globalExists = globalCacheMap.contains(uniqueId);
        if (globalExists)
            return true;

        return storageMap.contains(uniqueId);
    }

    @Override
    public @NotNull PipelineStream<UUID> keys() {
        return storageMap.keys();
    }

    @Override
    public @NotNull PipelineStream<Document> values() {
        return storageMap.values();
    }

    @Override
    public @NotNull PipelineStream<Pair<UUID, Document>> entries() {
        return storageMap.entries();
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        localCacheMap.remove(uniqueId);
        //TODO: Push data Updater
        globalCacheMap.remove(uniqueId);
        storageMap.remove(uniqueId);
    }

    @Override
    public void clear() {

    }

    @Override
    public long size() {
        return storageMap.size();
    }
}
