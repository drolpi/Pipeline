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
import de.natrox.pipeline.part.config.GlobalCacheConfig;
import de.natrox.pipeline.part.config.GlobalStorageConfig;
import de.natrox.pipeline.part.config.LocalCacheConfig;
import de.natrox.pipeline.part.config.LocalStorageConfig;
import de.natrox.pipeline.part.provider.GlobalCacheProvider;
import de.natrox.pipeline.part.provider.GlobalStorageProvider;
import de.natrox.pipeline.part.provider.LocalCacheProvider;
import de.natrox.pipeline.part.provider.LocalStorageProvider;
import de.natrox.pipeline.part.provider.UpdaterProvider;
import de.natrox.pipeline.part.store.Store;
import de.natrox.pipeline.part.updater.Updater;
import org.jetbrains.annotations.NotNull;

final class PipelineBuilderImpl {

    @SuppressWarnings("unchecked")
    abstract static class AbstractBuilder<R extends Pipeline.Builder<R>> implements Pipeline.Builder<R> {

        protected GlobalCacheProvider globalCacheProvider;
        protected GlobalCacheConfig globalCacheConfig;
        protected LocalCacheProvider localCacheProvider;
        protected LocalCacheConfig localCacheConfig;

        protected AbstractBuilder() {

        }

        @Override
        public @NotNull R globalCache(@NotNull GlobalCacheProvider provider, @NotNull GlobalCacheConfig config) {
            Check.notNull(provider, "provider");
            Check.notNull(config, "config");
            this.globalCacheProvider = provider;
            this.globalCacheConfig = config;
            return (R) this;
        }
    }

    final static class GlobalBuilderImpl extends AbstractBuilder<Pipeline.GlobalBuilder> implements Pipeline.GlobalBuilder {

        private final GlobalStorageProvider globalStorageProvider;
        private final GlobalStorageConfig globalStorageConfig;
        private UpdaterProvider updaterProvider;

        GlobalBuilderImpl(GlobalStorageProvider globalStorageProvider, GlobalStorageConfig globalStorageConfig) {
            this.globalStorageProvider = globalStorageProvider;
            this.globalStorageConfig = globalStorageConfig;
        }

        @Override
        public @NotNull Pipeline.GlobalBuilder localCache(
            @NotNull LocalCacheProvider localCacheProvider,
            @NotNull UpdaterProvider updaterProvider,
            @NotNull LocalCacheConfig config
        ) {
            Check.notNull(localCacheProvider, "localCacheProvider");
            Check.notNull(updaterProvider, "updaterProvider");
            Check.notNull(config, "config");
            this.localCacheProvider = localCacheProvider;
            this.localCacheConfig = config;
            this.updaterProvider = updaterProvider;
            return this;
        }

        @Override
        public Pipeline build() {
            final Store storage = this.globalStorageProvider.createGlobalStorage(this.globalStorageConfig);
            final Store globalCache = this.globalCacheProvider != null ? this.globalCacheProvider.createGlobalCache(this.globalCacheConfig) : null;

            final boolean local = this.updaterProvider != null && this.localCacheProvider != null;
            final Store localCache = local ? this.localCacheProvider.createLocalCache(this.localCacheConfig) : null;
            final Updater updater = local ? this.updaterProvider.createDataUpdater() : null;

            return new PipelineImpl(storage, globalCache, localCache, updater);
        }
    }

    final static class LocalBuilderImpl extends AbstractBuilder<Pipeline.LocalBuilder> implements Pipeline.LocalBuilder {

        private final LocalStorageProvider localStorageProvider;
        private final LocalStorageConfig localStorageConfig;

        LocalBuilderImpl(LocalStorageProvider localStorageProvider, LocalStorageConfig localStorageConfig) {
            this.localStorageProvider = localStorageProvider;
            this.localStorageConfig = localStorageConfig;
        }

        @Override
        public @NotNull Pipeline.LocalBuilder localCache(@NotNull LocalCacheProvider provider, @NotNull LocalCacheConfig config) {
            Check.notNull(provider, "provider");
            Check.notNull(config, "config");
            this.localCacheProvider = provider;
            this.localCacheConfig = config;
            return this;
        }

        @Override
        public Pipeline build() {
            final Store storage = this.localStorageProvider.createLocalStorage(this.localStorageConfig);
            final Store globalCache = this.globalCacheProvider != null ? this.globalCacheProvider.createGlobalCache(this.globalCacheConfig) : null;
            final Store localCache = this.localCacheProvider != null ? this.localCacheProvider.createLocalCache(this.localCacheConfig) : null;

            return new PipelineImpl(storage, globalCache, localCache, null);
        }
    }
}
