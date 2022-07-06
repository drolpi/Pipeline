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
import de.natrox.pipeline.concurrent.LockService;
import de.natrox.pipeline.exception.PipelineException;
import de.natrox.pipeline.mapper.DocumentMapper;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.object.annotation.AnnotationResolver;
import de.natrox.pipeline.part.config.GlobalCacheConfig;
import de.natrox.pipeline.part.config.GlobalStorageConfig;
import de.natrox.pipeline.part.config.LocalCacheConfig;
import de.natrox.pipeline.part.config.LocalStorageConfig;
import de.natrox.pipeline.part.provider.*;
import de.natrox.pipeline.part.store.Store;
import de.natrox.pipeline.part.updater.Updater;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

final class PipelineImpl implements Pipeline {

    private final @Nullable Updater updater;

    private final DocumentMapper documentMapper;
    private final DocumentRepositoryFactory documentRepositoryFactory;
    private final ObjectRepositoryFactory objectRepositoryFactory;

    private PipelineStore pipelineStore;

    PipelineImpl(@NotNull Store storage, @Nullable Store globalCache, @Nullable Store localCache, @Nullable Updater updater) {
        Check.notNull(storage, "storage");
        this.updater = updater;
        this.pipelineStore = new PipelineStore(this, storage, globalCache, localCache, updater);
        this.documentMapper = DocumentMapper.create();

        LockService lockService = new LockService();
        this.documentRepositoryFactory = new DocumentRepositoryFactory(this, this.pipelineStore, lockService);
        this.objectRepositoryFactory = new ObjectRepositoryFactory(this, this.documentRepositoryFactory);
    }

    @Override
    public @NotNull DocumentRepository repository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        return this.documentRepositoryFactory.repository(name);
    }

    @Override
    public @NotNull DocumentRepository.Builder buildRepository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        return new DocumentRepositoryImpl.BuilderImpl(this.documentRepositoryFactory, name);
    }

    @Override
    public @NotNull <T extends ObjectData> ObjectRepository<T> repository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        this.checkOpened();
        return this.objectRepositoryFactory.repository(type);
    }

    @Override
    public <T extends ObjectData> ObjectRepository.@NotNull Builder<T> buildRepository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        this.checkOpened();
        return new ObjectRepositoryImpl.BuilderImpl<>(this.objectRepositoryFactory, type);
    }

    @Override
    public boolean hasRepository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        return this.pipelineStore.hasMap(name);
    }

    @Override
    public <T> boolean hasRepository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        this.checkOpened();
        return this.pipelineStore.hasMap(AnnotationResolver.identifier(type));
    }

    @Override
    public void destroyRepository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        this.pipelineStore.removeMap(name);
    }

    @Override
    public <T extends ObjectData> void destroyRepository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        this.checkOpened();
        this.pipelineStore.removeMap(AnnotationResolver.identifier(type));
    }

    @Override
    public @NotNull Set<String> repositories() {
        this.checkOpened();
        return this.pipelineStore.maps();
    }

    @Override
    public @NotNull DocumentMapper documentMapper() {
        return this.documentMapper;
    }

    @Override
    public boolean isClosed() {
        return this.pipelineStore == null || pipelineStore.isClosed();
    }

    @Override
    public void close() {
        this.pipelineStore.close();
        this.documentRepositoryFactory.clear();
        this.objectRepositoryFactory.clear();

        this.pipelineStore = null;
    }

    private void checkOpened() {
        if (!this.isClosed())
            return;
        throw new PipelineException("Pipeline is closed");
    }

    public @Nullable Updater updater() {
        return this.updater;
    }

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

    final static class GlobalBuilderImpl extends AbstractBuilder<GlobalBuilder> implements Pipeline.GlobalBuilder {

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

    final static class LocalBuilderImpl extends AbstractBuilder<LocalBuilder> implements Pipeline.LocalBuilder {

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
