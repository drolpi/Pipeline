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

import de.natrox.pipeline.part.Part;
import de.natrox.pipeline.part.cache.DataUpdater;
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.cache.LocalCache;
import de.natrox.pipeline.part.map.PartMap;
import de.natrox.pipeline.part.storage.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConnectingPart implements Part {

    private final Storage storage;
    private final @Nullable GlobalCache globalCache;
    private final @Nullable DataUpdater dataUpdater;
    private final @Nullable LocalCache localCache;

    public ConnectingPart(@NotNull Storage storage, @Nullable GlobalCache globalCache, @Nullable DataUpdater dataUpdater, @Nullable LocalCache localCache) {
        this.storage = storage;
        this.globalCache = globalCache;
        this.dataUpdater = dataUpdater;
        this.localCache = localCache;
    }

    @Override
    public @NotNull PartMap openMap(@NotNull String mapName) {
        PartMap localCacheMap = PartMap.EMPTY;
        if (localCache != null) {
            localCacheMap = localCache.openMap(mapName);
        }

        PartMap globalCacheMap = PartMap.EMPTY;
        if (globalCache != null) {
            globalCacheMap = globalCache.openMap(mapName);
        }

        PartMap storageMap = storage.openMap(mapName);

        return new ConnectingMap(storageMap, globalCacheMap, localCacheMap, dataUpdater);
    }

    @Override
    public boolean hasMap(String mapName) {
        //TODO: Maybe check other parts too
        return storage.hasMap(mapName);
    }


    @Override
    public void closeMap(String mapName) {
        if (localCache != null) {
            localCache.closeMap(mapName);
        }

        if (globalCache != null) {
            globalCache.closeMap(mapName);
        }

        storage.closeMap(mapName);
    }

    @Override
    public void removeMap(String mapName) {
        if (localCache != null) {
            localCache.removeMap(mapName);
        }

        if (globalCache != null) {
            globalCache.removeMap(mapName);
        }

        storage.removeMap(mapName);
    }
}
