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
import de.natrox.pipeline.part.cache.provider.DataUpdaterProvider;
import de.natrox.pipeline.part.cache.provider.GlobalCacheProvider;
import de.natrox.pipeline.part.cache.provider.LocalCacheProvider;
import de.natrox.pipeline.part.storage.provider.GlobalStorageProvider;
import de.natrox.pipeline.part.storage.provider.LocalStorageProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface PartBundle permits LocalBundle, GlobalBundle {

    static @NotNull PartBundle local(
        @NotNull LocalStorageProvider localStorageProvider,
        @NotNull LocalCacheProvider localCacheProvider
    ) {
        Check.notNull(localStorageProvider, "localStorageProvider");
        Check.notNull(localCacheProvider, "localCacheProvider");
        return new LocalBundle(localStorageProvider, localCacheProvider);
    }

    static @NotNull PartBundle local(@NotNull LocalStorageProvider localStorageProvider) {
        Check.notNull(localStorageProvider, "localStorageProvider");
        return new LocalBundle(localStorageProvider);
    }

    static @NotNull PartBundle global(
        @NotNull GlobalStorageProvider globalStorageProvider,
        @NotNull GlobalCacheProvider globalCacheProvider,
        @NotNull DataUpdaterProvider dataUpdaterProvider,
        @NotNull LocalCacheProvider localCacheProvider
    ) {
        return new GlobalBundle(globalStorageProvider, globalCacheProvider, dataUpdaterProvider, localCacheProvider);
    }

    static @NotNull PartBundle global(
        @NotNull GlobalStorageProvider globalStorageProvider,
        @NotNull DataUpdaterProvider dataUpdaterProvider,
        @NotNull LocalCacheProvider localCacheProvider
    ) {
        return new GlobalBundle(globalStorageProvider, dataUpdaterProvider, localCacheProvider);
    }

    static @NotNull PartBundle global(
        @NotNull GlobalStorageProvider globalStorageProvider,
        @NotNull GlobalCacheProvider globalCacheProvider
    ) {
        return new GlobalBundle(globalStorageProvider, globalCacheProvider);
    }

    static @NotNull PartBundle global(@NotNull GlobalStorageProvider globalStorageProvider) {
        return new GlobalBundle(globalStorageProvider);
    }

    @Nullable LocalCacheProvider localCacheProvider();

}
