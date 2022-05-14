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

import de.natrox.pipeline.part.Store;
import de.natrox.pipeline.part.PartMap;
import de.natrox.pipeline.part.cache.DataUpdater;
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.cache.LocalCache;
import de.natrox.pipeline.part.storage.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ClassCanBeRecord")
public final class ConnectingStore implements Store {

    private final Storage storage;
    private final @Nullable GlobalCache globalCache;
    private final @Nullable DataUpdater dataUpdater;
    private final @Nullable LocalCache localCache;

    public ConnectingStore(@NotNull Storage storage, @Nullable GlobalCache globalCache, @Nullable DataUpdater dataUpdater, @Nullable LocalCache localCache) {
        this.storage = storage;
        this.globalCache = globalCache;
        this.dataUpdater = dataUpdater;
        this.localCache = localCache;
    }

    @Override
    public @NotNull PartMap openMap(@NotNull String mapName) {
        PartMap localCacheMap = null;
        if (this.localCache != null) {
            localCacheMap = this.localCache.openMap(mapName);
        }

        PartMap globalCacheMap = null;
        if (this.globalCache != null) {
            globalCacheMap = this.globalCache.openMap(mapName);
        }

        PartMap storageMap = this.storage.openMap(mapName);

        return new ConnectingMap(storageMap, globalCacheMap, localCacheMap, this.dataUpdater);
    }

    @Override
    public boolean hasMap(@NotNull String mapName) {
        //TODO: Maybe check other parts too
        return this.storage.hasMap(mapName);
    }


    @Override
    public void closeMap(@NotNull String mapName) {
        if (this.localCache != null) {
            this.localCache.closeMap(mapName);
        }

        if (this.globalCache != null) {
            this.globalCache.closeMap(mapName);
        }

        this.storage.closeMap(mapName);
    }

    @Override
    public void removeMap(@NotNull String mapName) {
        if (this.localCache != null) {
            this.localCache.removeMap(mapName);
        }

        if (this.globalCache != null) {
            this.globalCache.removeMap(mapName);
        }

        this.storage.removeMap(mapName);
    }
}
