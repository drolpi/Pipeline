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

package de.natrox.pipeline.part.memory;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.config.LocalStorageConfig;
import de.natrox.pipeline.part.store.Store;
import de.natrox.pipeline.part.provider.LocalStorageProvider;
import org.jetbrains.annotations.NotNull;

public final class InMemoryProvider implements LocalStorageProvider {

    private InMemoryProvider() {

    }

    public static @NotNull InMemoryProvider create() {
        return new InMemoryProvider();
    }

    @Override
    public void close() {

    }

    @Override
    public @NotNull Store createLocalStorage(@NotNull Pipeline pipeline, @NotNull LocalStorageConfig config) {
        return new InMemoryStore();
    }
}
