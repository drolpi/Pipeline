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

import de.natrox.pipeline.object.InstanceCreator;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.part.config.GlobalCacheConfig;
import de.natrox.pipeline.part.config.LocalCacheConfig;
import de.natrox.pipeline.part.config.StorageConfig;
import org.jetbrains.annotations.NotNull;

public sealed abstract class RepositoryOptions {

    private final StorageConfig storageConfig;
    private final boolean useGlobalCache;
    private final GlobalCacheConfig globalCacheConfig;
    private final boolean useLocalCache;
    private final LocalCacheConfig localCacheConfig;

    RepositoryOptions(
        StorageConfig storageConfig,
        boolean useGlobalCache,
        GlobalCacheConfig globalCacheConfig,
        boolean useLocalCache,
        LocalCacheConfig localCacheConfig
    ) {
        this.storageConfig = storageConfig;
        this.useGlobalCache = useGlobalCache;
        this.globalCacheConfig = globalCacheConfig;
        this.useLocalCache = useLocalCache;
        this.localCacheConfig = localCacheConfig;
    }

    public @NotNull StorageConfig storageConfig() {
        return this.storageConfig;
    }

    public boolean useGlobalCache() {
        return this.useGlobalCache;
    }

    public @NotNull GlobalCacheConfig globalCacheConfig() {
        return this.globalCacheConfig;
    }

    public boolean useLocalCache() {
        return this.useLocalCache;
    }

    public @NotNull LocalCacheConfig localCacheConfig() {
        return this.localCacheConfig;
    }

    static non-sealed class DocumentOptions extends RepositoryOptions {

        DocumentOptions(
            StorageConfig storageConfig,
            boolean useGlobalCache,
            GlobalCacheConfig globalCacheConfig,
            boolean useLocalCache,
            LocalCacheConfig localCacheConfig
        ) {
            super(storageConfig, useGlobalCache, globalCacheConfig, useLocalCache, localCacheConfig);
        }
    }

    static final class ObjectOptions<T extends ObjectData> extends DocumentOptions {

        private final InstanceCreator<T> instanceCreator;

        ObjectOptions(
            StorageConfig storageConfig,
            boolean useGlobalCache,
            GlobalCacheConfig globalCacheConfig,
            boolean useLocalCache,
            LocalCacheConfig localCacheConfig,
            InstanceCreator<T> instanceCreator
        ) {
            super(storageConfig, useGlobalCache, globalCacheConfig, useLocalCache, localCacheConfig);
            this.instanceCreator = instanceCreator;
        }

        public InstanceCreator<T> instanceCreator() {
            return this.instanceCreator;
        }
    }
}
