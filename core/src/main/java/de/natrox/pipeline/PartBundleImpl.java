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

import de.natrox.pipeline.part.cache.DataUpdater;
import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.part.cache.LocalCache;
import de.natrox.pipeline.part.cache.provider.DataUpdaterProvider;
import de.natrox.pipeline.part.cache.provider.GlobalCacheProvider;
import de.natrox.pipeline.part.cache.provider.LocalCacheProvider;
import de.natrox.pipeline.part.connecting.ConnectingPart;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.LocalStorage;
import de.natrox.pipeline.part.storage.provider.GlobalStorageProvider;
import de.natrox.pipeline.part.storage.provider.LocalStorageProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class PartBundleImpl {

    private PartBundleImpl() {
        throw new UnsupportedOperationException();
    }

    record Local(@NotNull LocalStorageProvider localStorageProvider,
                 @Nullable LocalCacheProvider localCacheProvider) implements PartBundle {

        @Override
        public @NotNull ConnectingPart createConnectingPart(@NotNull Pipeline pipeline) {
            LocalStorage storage = this.localStorageProvider.constructLocalStorage(pipeline);
            LocalCache localCache = this.localCacheProvider != null ? this.localCacheProvider.constructLocalCache(pipeline) : null;

            return new ConnectingPart(storage, null, null, localCache);
        }

    }

    record Global(@NotNull GlobalStorageProvider globalStorageProvider,
                  @Nullable GlobalCacheProvider globalCacheProvider,
                  @Nullable DataUpdaterProvider dataUpdaterProvider,
                  @Nullable LocalCacheProvider localCacheProvider) implements PartBundle {

        @Override
        public @NotNull ConnectingPart createConnectingPart(@NotNull Pipeline pipeline) {
            GlobalStorage storage = this.globalStorageProvider.constructGlobalStorage(pipeline);
            GlobalCache globalCache = this.globalCacheProvider != null ? this.globalCacheProvider.constructGlobalCache(pipeline) : null;

            boolean local = this.dataUpdaterProvider != null && this.localCacheProvider != null;
            DataUpdater dataUpdater = local ? this.dataUpdaterProvider.constructDataUpdater(pipeline) : null;
            LocalCache localCache = local ? this.localCacheProvider.constructLocalCache(pipeline) : null;

            return new ConnectingPart(storage, globalCache, dataUpdater, localCache);
        }

    }

}
