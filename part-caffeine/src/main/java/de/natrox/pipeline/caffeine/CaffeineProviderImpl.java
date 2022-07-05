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

package de.natrox.pipeline.caffeine;

import de.natrox.pipeline.repository.Pipeline;
import de.natrox.pipeline.part.config.LocalCacheConfig;
import de.natrox.pipeline.part.store.Store;
import org.jetbrains.annotations.NotNull;

final class CaffeineProviderImpl implements CaffeineProvider {

    @Override
    public @NotNull Store createLocalCache(@NotNull Pipeline pipeline, @NotNull LocalCacheConfig config) {
        return new CaffeineStore(config);
    }

    @Override
    public void close() {

    }
}
