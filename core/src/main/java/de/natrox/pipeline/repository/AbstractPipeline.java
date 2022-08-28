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
import de.natrox.pipeline.part.config.StorageConfig;
import de.natrox.pipeline.part.provider.GlobalCacheProvider;
import de.natrox.pipeline.part.provider.LocalCacheProvider;
import de.natrox.pipeline.part.provider.PartProvider;
import de.natrox.pipeline.part.updater.Updater;
import de.natrox.pipeline.serializer.NodeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

sealed abstract class AbstractPipeline implements Pipeline permits GlobalPipeline, LocalPipeline {

    private final PartBundle<?> partBundle;
    private final @Nullable Updater updater;
    private final RepositoryFactory repositoryFactory;

    private PipelineStore pipelineStore;

    AbstractPipeline(@NotNull PartBundle<?> partBundle) {
        Check.notNull(partBundle, "partBundle");
        this.partBundle = partBundle;

        this.pipelineStore = partBundle.createStore(this);
        this.updater = this.pipelineStore.updater();

        LockService lockService = new LockService();
        this.repositoryFactory = new RepositoryFactory(this.pipelineStore, lockService);
    }

    @Override
    public @NotNull Repository repository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        return this.repositoryFactory.repository(name);
    }

    @Override
    public @NotNull Repository.Builder buildRepository(@NotNull String name, @NotNull StorageConfig config) {
        Check.notNull(name, "name");
        Check.notNull(config, "config");
        this.checkOpened();
        return new RepositoryBuilder(this.repositoryFactory, name, config);
    }

    @Override
    public boolean hasRepository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        return this.pipelineStore.hasMap(name);
    }

    @Override
    public void destroyRepository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        this.pipelineStore.removeMap(name);
    }

    @Override
    public @NotNull Set<String> repositories() {
        this.checkOpened();
        return this.pipelineStore.maps();
    }

    @Override
    public boolean isClosed() {
        return this.pipelineStore == null || this.pipelineStore.isClosed();
    }

    @Override
    public void close() {
        this.pipelineStore.close();
        this.repositoryFactory.clear();

        this.pipelineStore = null;
    }

    @Override
    public void closeProviders() {
        this.partBundle.close();
        this.close();
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
    static abstract class Builder<R extends Pipeline.Builder<R>> implements Pipeline.Builder<R> {

        protected GlobalCacheProvider globalCacheProvider;
        protected LocalCacheProvider localCacheProvider;

        protected Builder() {

        }

        @Override
        public @NotNull R globalCache(@NotNull GlobalCacheProvider provider) {
            Check.notNull(provider, "provider");
            this.globalCacheProvider = provider;
            return (R) this;
        }
    }

    static abstract class PartBundle<T extends PartProvider> {

        protected final T storageProvider;
        protected final GlobalCacheProvider globalCacheProvider;
        protected final LocalCacheProvider localCacheProvider;

        public PartBundle(T storageProvider, GlobalCacheProvider globalCacheProvider, LocalCacheProvider localCacheProvider) {
            this.storageProvider = storageProvider;
            this.globalCacheProvider = globalCacheProvider;
            this.localCacheProvider = localCacheProvider;
        }

        public abstract PipelineStore createStore(@NotNull AbstractPipeline pipeline);

        public abstract void close();

        public @NotNull T storageProvider() {
            return this.storageProvider;
        }

        public @Nullable GlobalCacheProvider globalCacheProvider() {
            return this.globalCacheProvider;
        }

        public @Nullable LocalCacheProvider localCacheProvider() {
            return this.localCacheProvider;
        }
    }

}
