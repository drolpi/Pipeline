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
import de.natrox.pipeline.part.config.LocalCacheConfig;
import de.natrox.pipeline.part.config.StorageConfig;
import de.natrox.pipeline.serializer.NodeSerializer;
import org.jetbrains.annotations.NotNull;

final class RepositoryBuilder implements Repository.Builder {

    private final RepositoryFactory factory;
    private final String name;
    private final StorageConfig storageConfig;
    private boolean useGlobalCache = false;
    private GlobalCacheConfig globalCacheConfig = GlobalCacheConfig.defaults();
    private boolean useLocalCache = false;
    private LocalCacheConfig localCacheConfig = LocalCacheConfig.defaults();
    private NodeSerializer nodeSerializer;

    RepositoryBuilder(RepositoryFactory factory, String name, StorageConfig storageConfig) {
        this.name = name;
        this.storageConfig = storageConfig;
        this.factory = factory;
    }

    @Override
    public Repository.@NotNull Builder withGlobalCache(@NotNull GlobalCacheConfig config) {
        Check.notNull(config, "config");
        this.globalCacheConfig = config;
        this.useGlobalCache = true;
        return this;
    }

    @Override
    public Repository.@NotNull Builder withLocalCache(@NotNull LocalCacheConfig config) {
        Check.notNull(config, "config");
        this.localCacheConfig = config;
        this.useLocalCache = true;
        return this;
    }

    @Override
    public Repository.@NotNull Builder serializer(@NotNull NodeSerializer nodeSerializer) {
        Check.notNull(nodeSerializer, "nodeSerializer");
        this.nodeSerializer = nodeSerializer;
        return this;
    }

    @Override
    public Repository build() {
        return this.factory.createRepository(this.name, new RepositoryOptions(
            this.storageConfig,
            this.useGlobalCache,
            this.globalCacheConfig,
            this.useLocalCache,
            this.localCacheConfig,
            this.nodeSerializer
        ));
    }
}
