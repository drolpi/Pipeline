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

import de.natrox.pipeline.part.connecting.ConnectingStore;
import de.natrox.pipeline.part.provider.local.LocalCacheUpdaterProvider;
import de.natrox.pipeline.part.provider.global.GlobalCacheProvider;
import de.natrox.pipeline.part.provider.global.GlobalStorageProvider;
import de.natrox.pipeline.part.provider.local.LocalCacheProvider;
import de.natrox.pipeline.part.provider.local.LocalStorageProvider;
import de.natrox.pipeline.part.Store;
import de.natrox.pipeline.part.LocalCacheUpdater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class PartBundleImpl {

    private PartBundleImpl() {
        throw new UnsupportedOperationException();
    }

    record Local(@NotNull LocalStorageProvider localStorageProvider,
                 @Nullable LocalCacheProvider localCacheProvider) implements PartBundle {

        @Override
        public @NotNull ConnectingStore createConnectingPart(@NotNull Pipeline pipeline) {
            Store storage = this.localStorageProvider.createLocalStorage(pipeline);
            Store localCache = this.localCacheProvider != null ? this.localCacheProvider.createLocalCache(pipeline) : null;

            return new ConnectingStore(storage, null, localCache, null);
        }

    }

    record Global(@NotNull GlobalStorageProvider globalStorageProvider,
                  @Nullable GlobalCacheProvider globalCacheProvider,
                  @Nullable LocalCacheProvider localCacheProvider,
                  @Nullable LocalCacheUpdaterProvider localCacheUpdaterProvider) implements PartBundle {

        @Override
        public @NotNull ConnectingStore createConnectingPart(@NotNull Pipeline pipeline) {
            Store storage = this.globalStorageProvider.createGlobalStorage(pipeline);
            Store globalCache = this.globalCacheProvider != null ? this.globalCacheProvider.createGlobalCache(pipeline) : null;

            boolean local = this.localCacheUpdaterProvider != null && this.localCacheProvider != null;
            Store localCache = local ? this.localCacheProvider.createLocalCache(pipeline) : null;
            LocalCacheUpdater localCacheUpdater = local ? this.localCacheUpdaterProvider.createLocalCacheUpdater(pipeline) : null;

            return new ConnectingStore(storage, globalCache, localCache, localCacheUpdater);
        }

    }

}
