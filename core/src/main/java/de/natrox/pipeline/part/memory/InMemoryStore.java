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

import de.natrox.pipeline.part.AbstractStore;
import de.natrox.pipeline.part.StoreMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class InMemoryStore extends AbstractStore {

    private final Map<String, InMemoryMap> inMemoryMapRegistry;
    private volatile boolean closed = false;

    InMemoryStore() {
        this.inMemoryMapRegistry = new ConcurrentHashMap<>();
    }

    @Override
    public @NotNull StoreMap openMap(@NotNull String mapName) {
        if (this.inMemoryMapRegistry.containsKey(mapName)) {
            return this.inMemoryMapRegistry.get(mapName);
        }
        InMemoryMap inMemoryMap = new InMemoryMap();
        this.inMemoryMapRegistry.put(mapName, inMemoryMap);

        return inMemoryMap;
    }

    @Override
    public boolean hasMap(@NotNull String mapName) {
        return this.inMemoryMapRegistry.containsKey(mapName);
    }

    @Override
    public void closeMap(@NotNull String mapName) {
        // nothing to do
    }

    @Override
    public void removeMap(@NotNull String mapName) {
        if (this.inMemoryMapRegistry.containsKey(mapName)) {
            InMemoryMap inMemoryMap = this.inMemoryMapRegistry.get(mapName);
            inMemoryMap.clear();
            this.inMemoryMapRegistry.remove(mapName);
        }
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        this.closed = true;

        this.inMemoryMapRegistry.clear();
    }
}
