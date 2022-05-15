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

package de.natrox.pipeline.part.memory;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.Store;
import de.natrox.pipeline.part.provider.local.LocalCacheProvider;
import de.natrox.pipeline.part.provider.local.LocalStorageProvider;
import org.jetbrains.annotations.NotNull;

public final class InMemoryProvider implements LocalCacheProvider, LocalStorageProvider {

    private InMemoryProvider() {

    }

    public static @NotNull InMemoryProvider create() {
        return new InMemoryProvider();
    }

    @Override
    public void close() {

    }

    @Override
    public @NotNull Store createLocalCache(@NotNull Pipeline pipeline) {
        return new InMemoryStore();
    }

    @Override
    public @NotNull Store createLocalStorage(@NotNull Pipeline pipeline) {
        return new InMemoryStore();
    }
}
