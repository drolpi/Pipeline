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
import de.natrox.pipeline.part.provider.GlobalCacheProvider;
import de.natrox.pipeline.part.provider.GlobalStorageProvider;
import de.natrox.pipeline.part.provider.LocalCacheProvider;
import de.natrox.pipeline.part.provider.UpdaterProvider;
import de.natrox.pipeline.part.store.Store;
import de.natrox.pipeline.part.updater.Updater;
import org.jetbrains.annotations.NotNull;

final class GlobalPipeline extends AbstractPipeline {

    private GlobalPipeline(PartBundle partBundle) {
        super(partBundle);
    }

    final static class Builder extends AbstractPipeline.Builder<GlobalBuilder> implements Pipeline.GlobalBuilder {

        private final GlobalStorageProvider storageProvider;
        private UpdaterProvider updaterProvider;

        Builder(GlobalStorageProvider storageProvider) {
            this.storageProvider = storageProvider;
        }

        @Override
        public @NotNull Pipeline.GlobalBuilder localCache(@NotNull LocalCacheProvider localCacheProvider, @NotNull UpdaterProvider updaterProvider) {
            Check.notNull(localCacheProvider, "localCacheProvider");
            Check.notNull(updaterProvider, "updaterProvider");
            this.localCacheProvider = localCacheProvider;
            this.updaterProvider = updaterProvider;
            return this;
        }

        @Override
        public Pipeline build() {
            PartBundle partBundle = new PartBundle(this.storageProvider, this.globalCacheProvider, this.localCacheProvider, this.updaterProvider);

            return new GlobalPipeline(partBundle);
        }
    }

    final static class PartBundle extends AbstractPipeline.PartBundle<GlobalStorageProvider> {

        private final UpdaterProvider updaterProvider;

        PartBundle(GlobalStorageProvider storageProvider, GlobalCacheProvider globalCacheProvider, LocalCacheProvider localCacheProvider, UpdaterProvider updaterProvider) {
            super(storageProvider, globalCacheProvider, localCacheProvider);
            this.updaterProvider = updaterProvider;
        }

        @Override
        public PipelineStore createStore(@NotNull AbstractPipeline pipeline) {
            final Store storage = this.storageProvider.createGlobalStorage();
            final Store globalCache = this.globalCacheProvider != null ? this.globalCacheProvider.createGlobalCache() : null;

            final boolean local = this.updaterProvider != null && this.localCacheProvider != null;
            final Store localCache = local ? this.localCacheProvider.createLocalCache() : null;
            final Updater updater = local ? this.updaterProvider.createDataUpdater() : null;

            return new PipelineStore(pipeline, storage, globalCache, localCache, updater);
        }

        @Override
        public void close() {
            this.storageProvider.close();

            if (this.globalCacheProvider != null) {
                this.globalCacheProvider.close();
            }

            if (this.localCacheProvider != null) {
                this.localCacheProvider.close();
            }

            if (this.updaterProvider != null) {
                this.updaterProvider.close();
            }
        }
    }
}
