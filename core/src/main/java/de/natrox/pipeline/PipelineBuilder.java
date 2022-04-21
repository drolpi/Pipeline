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
import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.json.JsonProvider;
import de.natrox.pipeline.part.cache.GlobalCacheProvider;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import de.natrox.pipeline.part.updater.DataUpdaterProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class PipelineBuilder implements Pipeline.Builder {

    private PipelineRegistry pipelineRegistry;
    private DataUpdaterProvider dataUpdaterConnection;
    private GlobalCacheProvider globalCacheConnection;
    private GlobalStorageProvider globalStorageConnection;
    private JsonProvider jsonProvider;

    protected PipelineBuilder() {

    }

    @Override
    public Pipeline.@NotNull Builder registry(@NotNull PipelineRegistry registry) {
        Check.notNull(registry, "registry");
        this.pipelineRegistry = registry;
        return this;
    }

    @Override
    public Pipeline.@NotNull Builder jsonProvider(@NotNull JsonProvider jsonProvider) {
        Check.notNull(jsonProvider, "jsonProvider");
        this.jsonProvider = jsonProvider;
        return this;
    }

    @Override
    public Pipeline.@NotNull Builder dataUpdater(@Nullable DataUpdaterProvider connection) {
        this.dataUpdaterConnection = connection;
        return this;
    }

    @Override
    public Pipeline.@NotNull Builder globalCache(@Nullable GlobalCacheProvider connection) {
        this.globalCacheConnection = connection;
        return this;
    }

    @Override
    public Pipeline.@NotNull Builder globalStorage(@Nullable GlobalStorageProvider connection) {
        this.globalStorageConnection = connection;
        return this;
    }

    @Override
    public @NotNull Pipeline build() {
        return new PipelineImpl(
            dataUpdaterConnection,
            globalCacheConnection,
            globalStorageConnection,
            jsonProvider,
            pipelineRegistry
        );
    }
}
