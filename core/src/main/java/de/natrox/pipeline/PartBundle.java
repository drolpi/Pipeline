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

package de.natrox.pipeline;

import de.natrox.pipeline.part.Store;
import de.natrox.pipeline.part.connecting.ConnectingStore;
import de.natrox.pipeline.part.provider.GlobalCacheProvider;
import de.natrox.pipeline.part.provider.GlobalStorageProvider;
import de.natrox.pipeline.part.provider.LocalCacheProvider;
import de.natrox.pipeline.part.provider.LocalStorageProvider;
import de.natrox.pipeline.part.provider.UpdaterProvider;
import de.natrox.pipeline.part.updater.Updater;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
sealed interface PartBundle permits PartBundle.Local, PartBundle.Global {

    @NotNull ConnectingStore createConnectingStore(@NotNull Pipeline pipeline);

    record Local(@NotNull LocalStorageProvider localStorageProvider,
                 @Nullable LocalCacheProvider localCacheProvider) implements PartBundle {

        @Override
        public @NotNull ConnectingStore createConnectingStore(@NotNull Pipeline pipeline) {
            Store storage = this.localStorageProvider.createLocalStorage(pipeline);
            Store localCache = this.localCacheProvider != null ? this.localCacheProvider.createLocalCache(pipeline) : null;

            return new ConnectingStore(pipeline, storage, null, localCache, null);
        }

    }

    record Global(@NotNull GlobalStorageProvider globalStorageProvider,
                  @Nullable GlobalCacheProvider globalCacheProvider,
                  @Nullable LocalCacheProvider localCacheProvider,
                  @Nullable UpdaterProvider updaterProvider) implements PartBundle {

        @Override
        public @NotNull ConnectingStore createConnectingStore(@NotNull Pipeline pipeline) {
            Store storage = this.globalStorageProvider.createGlobalStorage(pipeline);
            Store globalCache = this.globalCacheProvider != null ? this.globalCacheProvider.createGlobalCache(pipeline) : null;

            boolean local = this.updaterProvider != null && this.localCacheProvider != null;
            Store localCache = local ? this.localCacheProvider.createLocalCache(pipeline) : null;
            Updater updater = local ? this.updaterProvider.createDataUpdater(pipeline) : null;

            return new ConnectingStore(pipeline, storage, globalCache, localCache, updater);
        }

    }

}
