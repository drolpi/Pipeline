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

package de.natrox.pipeline;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.StoreManager;
import de.natrox.pipeline.part.cache.DataUpdater;
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.cache.LocalCache;
import de.natrox.pipeline.part.cache.provider.DataUpdaterProvider;
import de.natrox.pipeline.part.cache.provider.GlobalCacheProvider;
import de.natrox.pipeline.part.cache.provider.LocalCacheProvider;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.LocalStorage;
import de.natrox.pipeline.part.storage.provider.GlobalStorageProvider;
import de.natrox.pipeline.part.storage.provider.LocalStorageProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface PartBundle permits PartBundle.Local, PartBundle.Global {

    static @NotNull PartBundle local(
        @NotNull LocalStorageProvider localStorageProvider,
        @NotNull LocalCacheProvider localCacheProvider
    ) {
        Check.notNull(localStorageProvider, "localStorageProvider");
        Check.notNull(localCacheProvider, "localCacheProvider");
        return new Local(localStorageProvider, localCacheProvider);
    }

    static @NotNull PartBundle local(@NotNull LocalStorageProvider localStorageProvider) {
        Check.notNull(localStorageProvider, "localStorageProvider");
        return new Local(localStorageProvider);
    }

    static @NotNull PartBundle global(
        @NotNull GlobalStorageProvider globalStorageProvider,
        @NotNull GlobalCacheProvider globalCacheProvider,
        @NotNull DataUpdaterProvider dataUpdaterProvider,
        @NotNull LocalCacheProvider localCacheProvider
    ) {
        return new Global(globalStorageProvider, globalCacheProvider, dataUpdaterProvider, localCacheProvider);
    }

    static @NotNull PartBundle global(
        @NotNull GlobalStorageProvider globalStorageProvider,
        @NotNull DataUpdaterProvider dataUpdaterProvider,
        @NotNull LocalCacheProvider localCacheProvider
    ) {
        return new Global(globalStorageProvider, dataUpdaterProvider, localCacheProvider);
    }

    static @NotNull PartBundle global(
        @NotNull GlobalStorageProvider globalStorageProvider,
        @NotNull GlobalCacheProvider globalCacheProvider
    ) {
        return new Global(globalStorageProvider, globalCacheProvider);
    }

    static @NotNull PartBundle global(@NotNull GlobalStorageProvider globalStorageProvider) {
        return new Global(globalStorageProvider);
    }

    @NotNull StoreManager createStoreManager();

    final class Local implements PartBundle {

        private final LocalStorageProvider localStorageProvider;
        private final LocalCacheProvider localCacheProvider;

        protected Local(
            @NotNull LocalStorageProvider localStorageProvider,
            @Nullable LocalCacheProvider localCacheProvider
        ) {
            this.localStorageProvider = localStorageProvider;
            this.localCacheProvider = localCacheProvider;
        }

        Local(LocalStorageProvider localStorageProvider) {
            this(localStorageProvider, null);
        }

        @Override
        public @NotNull StoreManager createStoreManager() {
            LocalStorage storage = this.localStorageProvider.constructLocalStorage();

            LocalCache localCache = null;
            if (this.localCacheProvider != null)
                localCache = this.localCacheProvider.constructLocalCache();

            return new StoreManager(storage, null, null, localCache);
        }

    }

    final class Global implements PartBundle {

        private final GlobalStorageProvider globalStorageProvider;
        private final GlobalCacheProvider globalCacheProvider;
        private final DataUpdaterProvider dataUpdaterProvider;
        private final LocalCacheProvider localCacheProvider;

        Global(
            @NotNull GlobalStorageProvider globalStorageProvider,
            @Nullable GlobalCacheProvider globalCacheProvider,
            @Nullable DataUpdaterProvider dataUpdaterProvider,
            @Nullable LocalCacheProvider localCacheProvider
        ) {
            this.globalStorageProvider = globalStorageProvider;
            this.globalCacheProvider = globalCacheProvider;
            this.dataUpdaterProvider = dataUpdaterProvider;
            this.localCacheProvider = localCacheProvider;
        }

        public Global(
            @NotNull GlobalStorageProvider globalStorageProvider,
            @Nullable DataUpdaterProvider dataUpdaterProvider,
            @Nullable LocalCacheProvider localCacheProvider
        ) {
            this(globalStorageProvider, null, dataUpdaterProvider, localCacheProvider);
        }

        public Global(
            @NotNull GlobalStorageProvider globalStorageProvider,
            @Nullable GlobalCacheProvider globalCacheProvider
        ) {
            this(globalStorageProvider, globalCacheProvider, null, null);
        }

        public Global(@NotNull GlobalStorageProvider globalStorageProvider) {
            this(globalStorageProvider, null);
        }

        @Override
        public @NotNull StoreManager createStoreManager() {
            GlobalStorage storage = globalStorageProvider.constructGlobalStorage();

            GlobalCache globalCache = null;
            if (globalCacheProvider != null)
                globalCache = globalCacheProvider.constructGlobalCache();

            DataUpdater dataUpdater = null;
            LocalCache localCache = null;
            if (dataUpdaterProvider != null && localCacheProvider != null) {
                dataUpdater = dataUpdaterProvider.constructDataUpdater();
                localCache = localCacheProvider.constructLocalCache();
            }

            return new StoreManager(storage, globalCache, dataUpdater, localCache);
        }

    }

}
