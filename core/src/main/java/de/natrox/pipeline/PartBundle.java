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
import de.natrox.pipeline.part.connecting.ConnectingStore;
import de.natrox.pipeline.part.provider.LocalUpdaterProvider;
import de.natrox.pipeline.part.provider.GlobalCacheProvider;
import de.natrox.pipeline.part.provider.GlobalStorageProvider;
import de.natrox.pipeline.part.provider.LocalCacheProvider;
import de.natrox.pipeline.part.provider.LocalStorageProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public sealed interface PartBundle permits PartBundleImpl.Local, PartBundleImpl.Global {

    static @NotNull PartBundle local(
        @NotNull LocalStorageProvider localStorageProvider,
        @NotNull LocalCacheProvider localCacheProvider
    ) {
        Check.notNull(localStorageProvider, "localStorageProvider");
        Check.notNull(localCacheProvider, "localCacheProvider");
        return new PartBundleImpl.Local(localStorageProvider, localCacheProvider);
    }

    static @NotNull PartBundle local(@NotNull LocalStorageProvider localStorageProvider) {
        Check.notNull(localStorageProvider, "localStorageProvider");
        return new PartBundleImpl.Local(localStorageProvider, null);
    }

    static @NotNull PartBundle global(
        @NotNull GlobalStorageProvider globalStorageProvider,
        @NotNull GlobalCacheProvider globalCacheProvider,
        @NotNull LocalCacheProvider localCacheProvider,
        @NotNull LocalUpdaterProvider localUpdaterProvider
    ) {
        return new PartBundleImpl.Global(globalStorageProvider, globalCacheProvider, localCacheProvider, localUpdaterProvider);
    }

    static @NotNull PartBundle global(
        @NotNull GlobalStorageProvider globalStorageProvider,
        @NotNull LocalCacheProvider localCacheProvider,
        @NotNull LocalUpdaterProvider localUpdaterProvider
    ) {
        return new PartBundleImpl.Global(globalStorageProvider, null, localCacheProvider, localUpdaterProvider);
    }

    static @NotNull PartBundle global(
        @NotNull GlobalStorageProvider globalStorageProvider,
        @NotNull GlobalCacheProvider globalCacheProvider
    ) {
        return new PartBundleImpl.Global(globalStorageProvider, globalCacheProvider, null, null);
    }

    static @NotNull PartBundle global(@NotNull GlobalStorageProvider globalStorageProvider) {
        return new PartBundleImpl.Global(globalStorageProvider, null, null, null);
    }

    @NotNull ConnectingStore createConnectingPart(@NotNull Pipeline pipeline);

}
