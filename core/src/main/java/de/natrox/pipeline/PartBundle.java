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

import de.natrox.pipeline.part.cache.provider.DataUpdaterProvider;
import de.natrox.pipeline.part.cache.provider.GlobalCacheProvider;
import de.natrox.pipeline.part.cache.provider.LocalCacheProvider;
import de.natrox.pipeline.part.storage.provider.GlobalStorageProvider;
import de.natrox.pipeline.part.storage.provider.LocalStorageProvider;

public sealed interface PartBundle permits LocalBundleImpl, GlobalBundleImpl {

    static PartBundle local(LocalCacheProvider localCache, LocalStorageProvider localStorage) {
        return new LocalBundleImpl(localCache, localStorage);
    }

    static PartBundle local(LocalStorageProvider localStorage) {
        return new LocalBundleImpl(localStorage);
    }

    static PartBundle global(LocalCacheProvider localCache, DataUpdaterProvider dataUpdater, GlobalCacheProvider globalCache, GlobalStorageProvider globalStorage) {
        return new GlobalBundleImpl(localCache, dataUpdater, globalCache, globalStorage);
    }

    static PartBundle global(LocalCacheProvider localCache, DataUpdaterProvider dataUpdater, GlobalStorageProvider globalStorage) {
        return new GlobalBundleImpl(localCache, dataUpdater, globalStorage);
    }

    static PartBundle global(GlobalCacheProvider globalCache, GlobalStorageProvider globalStorage) {
        return new GlobalBundleImpl(globalCache, globalStorage);
    }

    static PartBundle global(GlobalStorageProvider globalStorage) {
        return new GlobalBundleImpl(globalStorage);
    }

}
