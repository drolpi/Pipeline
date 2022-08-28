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

import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.store.Store;
import de.natrox.pipeline.part.store.StoreMap;
import de.natrox.pipeline.part.updater.Updater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

final class PipelineStore implements Store {

    private final Store storage;
    private final @Nullable Store globalCache;
    private final @Nullable Store localCache;
    private final @Nullable Updater updater;

    public PipelineStore(@NotNull Pipeline pipeline, @NotNull Store storage, @Nullable Store globalCache, @Nullable Store localCache, @Nullable Updater updater) {
        this.storage = storage;
        this.globalCache = globalCache;
        this.localCache = localCache;
        this.updater = updater;
    }

    @Override
    public @NotNull PipelineMap openMap(@NotNull String mapName, @NotNull RepositoryOptions repositoryOptions) {
        Check.notNull(mapName, "mapName");
        StoreMap localCacheMap = null;
        Updater updater = null;
        if (repositoryOptions.useLocalCache() && this.localCache != null) {
            localCacheMap = this.localCache.openMap(mapName, repositoryOptions);
            updater = this.updater;
        }

        StoreMap globalCacheMap = null;
        if (repositoryOptions.useGlobalCache() && this.globalCache != null) {
            globalCacheMap = this.globalCache.openMap(mapName, repositoryOptions);
        }

        StoreMap storageMap = this.storage.openMap(mapName, repositoryOptions);

        return new PipelineMap(mapName, storageMap, globalCacheMap, localCacheMap, updater);
    }

    @Override
    public @NotNull Set<String> maps() {
        return this.storage.maps();
    }

    @Override
    public boolean hasMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        //TODO: Maybe check other parts too
        return this.storage.hasMap(mapName);
    }

    @Override
    public void closeMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
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
        Check.notNull(mapName, "mapName");
        if (this.localCache != null) {
            this.localCache.removeMap(mapName);
        }

        if (this.globalCache != null) {
            this.globalCache.removeMap(mapName);
        }

        this.storage.removeMap(mapName);
    }

    @Override
    public boolean isClosed() {
        return this.storage.isClosed()
            || (this.globalCache != null && this.globalCache.isClosed())
            || (this.localCache != null && this.localCache.isClosed());
    }

    @Override
    public void close() {
        if (this.localCache != null) {
            this.localCache.close();
        }
        if (this.globalCache != null) {
            this.globalCache.close();
        }
        this.storage.close();
    }

    public @Nullable Updater updater() {
        return this.updater;
    }
}
