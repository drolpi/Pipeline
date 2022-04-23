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

import de.natrox.pipeline.part.StoreManager;
import de.natrox.pipeline.part.cache.LocalCache;
import de.natrox.pipeline.part.cache.provider.LocalCacheProvider;
import de.natrox.pipeline.part.storage.LocalStorage;
import de.natrox.pipeline.part.storage.provider.LocalStorageProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class LocalBundle implements PartBundle {

    private final LocalStorageProvider localStorageProvider;
    private final LocalCacheProvider localCacheProvider;

    protected LocalBundle(
        @NotNull LocalStorageProvider localStorageProvider,
        @Nullable LocalCacheProvider localCacheProvider
    ) {
        this.localStorageProvider = localStorageProvider;
        this.localCacheProvider = localCacheProvider;
    }

    public LocalBundle(LocalStorageProvider localStorageProvider) {
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
