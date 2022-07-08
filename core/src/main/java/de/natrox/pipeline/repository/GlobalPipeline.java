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
import de.natrox.pipeline.part.provider.GlobalStorageProvider;
import de.natrox.pipeline.part.provider.LocalCacheProvider;
import de.natrox.pipeline.part.provider.UpdaterProvider;
import de.natrox.pipeline.part.store.Store;
import de.natrox.pipeline.part.updater.Updater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class GlobalPipeline extends AbstractPipeline {

    private GlobalPipeline(@NotNull Store storage, @Nullable Store globalCache, @Nullable Store localCache, @Nullable Updater updater) {
        super(storage, globalCache, localCache, updater);
    }

    final static class Builder extends AbstractPipeline.Builder<GlobalBuilder> implements Pipeline.GlobalBuilder {

        private final GlobalStorageProvider globalStorageProvider;
        private UpdaterProvider updaterProvider;

        Builder(GlobalStorageProvider globalStorageProvider) {
            this.globalStorageProvider = globalStorageProvider;
        }

        @Override
        public @NotNull Pipeline.GlobalBuilder localCache(
            @NotNull LocalCacheProvider localCacheProvider,
            @NotNull UpdaterProvider updaterProvider
        ) {
            Check.notNull(localCacheProvider, "localCacheProvider");
            Check.notNull(updaterProvider, "updaterProvider");
            this.localCacheProvider = localCacheProvider;
            this.updaterProvider = updaterProvider;
            return this;
        }

        @Override
        public Pipeline build() {
            final Store storage = this.globalStorageProvider.createGlobalStorage();
            final Store globalCache = this.globalCacheProvider != null ? this.globalCacheProvider.createGlobalCache() : null;

            final boolean local = this.updaterProvider != null && this.localCacheProvider != null;
            final Store localCache = local ? this.localCacheProvider.createLocalCache() : null;
            final Updater updater = local ? this.updaterProvider.createDataUpdater() : null;

            return new GlobalPipeline(storage, globalCache, localCache, updater);
        }
    }
}
