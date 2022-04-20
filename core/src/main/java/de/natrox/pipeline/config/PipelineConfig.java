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

package de.natrox.pipeline.config;

import de.natrox.pipeline.config.part.PartConfig;
import de.natrox.pipeline.part.cache.GlobalCacheProvider;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import de.natrox.pipeline.part.updater.DataUpdaterProvider;
import org.jetbrains.annotations.Nullable;

public final class PipelineConfig {

    //TODO: Implement config loader

    private PartConfig<DataUpdaterProvider> dataUpdater;
    private PartConfig<GlobalCacheProvider> globalCache;
    private PartConfig<GlobalStorageProvider> globalStorage;

    public @Nullable PartConfig<DataUpdaterProvider> dataUpdater() {
        return this.dataUpdater;
    }

    public @Nullable PartConfig<GlobalCacheProvider> globalCache() {
        return this.globalCache;
    }

    public @Nullable PartConfig<GlobalStorageProvider> globalStorage() {
        return this.globalStorage;
    }
}
