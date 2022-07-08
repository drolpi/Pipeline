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
import de.natrox.pipeline.part.provider.LocalCacheProvider;
import de.natrox.pipeline.part.provider.LocalStorageProvider;
import de.natrox.pipeline.part.store.Store;
import org.jetbrains.annotations.NotNull;

final class LocalPipeline extends AbstractPipeline {

    private LocalPipeline(AbstractPipeline.@NotNull PartBundle<?> partBundle) {
        super(partBundle);
    }

    final static class Builder extends AbstractPipeline.Builder<LocalBuilder> implements Pipeline.LocalBuilder {

        private final LocalStorageProvider storageProvider;

        Builder(LocalStorageProvider storageProvider) {
            this.storageProvider = storageProvider;
        }

        @Override
        public @NotNull Pipeline.LocalBuilder localCache(@NotNull LocalCacheProvider provider) {
            Check.notNull(provider, "provider");
            this.localCacheProvider = provider;
            return this;
        }

        @Override
        public Pipeline build() {
            return new LocalPipeline(new PartBundle(this.storageProvider, this.globalCacheProvider, this.localCacheProvider));
        }
    }

    final static class PartBundle extends AbstractPipeline.PartBundle<LocalStorageProvider> {

        PartBundle(LocalStorageProvider storageProvider, GlobalCacheProvider globalCacheProvider, LocalCacheProvider localCacheProvider) {
            super(storageProvider, globalCacheProvider, localCacheProvider);
        }

        @Override
        public PipelineStore createStore(@NotNull AbstractPipeline pipeline) {
            final Store storage = this.storageProvider.createLocalStorage();
            final Store globalCache = this.globalCacheProvider != null ? this.globalCacheProvider.createGlobalCache() : null;
            final Store localCache = this.localCacheProvider != null ? this.localCacheProvider.createLocalCache() : null;

            return new PipelineStore(pipeline, storage, globalCache, localCache, null);
        }

        @Override
        public void close() {
            this.storageProvider.close();
            this.globalCacheProvider.close();
            this.localCacheProvider.close();
        }
    }
}
