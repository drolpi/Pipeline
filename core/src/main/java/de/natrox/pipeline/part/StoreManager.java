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

import de.natrox.pipeline.part.cache.DataUpdater;
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.cache.LocalCache;
import de.natrox.pipeline.part.storage.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StoreManager implements Part {

    private final Storage storage;
    private final @Nullable GlobalCache globalCache;
    private final @Nullable DataUpdater dataUpdater;
    private final @Nullable LocalCache localCache;

    private final DataSynchronizer dataSynchronizer;

    public StoreManager(@NotNull Storage storage, @Nullable GlobalCache globalCache, @Nullable DataUpdater dataUpdater, @Nullable LocalCache localCache) {
        this.storage = storage;
        this.globalCache = globalCache;
        this.dataUpdater = dataUpdater;
        this.localCache = localCache;

        this.dataSynchronizer = new DataSynchronizer();
    }


    @Override
    public PartMap openMap(String mapName) {
        PartMap localCacheMap = PartMap.EMPTY;
        if (localCache != null) {
            localCacheMap = localCache.openMap(mapName);
        }

        PartMap globalCacheMap = PartMap.EMPTY;
        if(globalCache != null) {
            globalCacheMap = globalCache.openMap(mapName);
        }

        PartMap storageMap = storage.openMap(mapName);

        return new DelegatingMap(storageMap, globalCacheMap, localCacheMap, dataUpdater);
    }
}
