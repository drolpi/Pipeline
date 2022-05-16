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

import java.util.Set;

final class InMemoryStore extends AbstractStore {

    InMemoryStore() {

    }

    @Override
    protected StoreMap createMap(@NotNull String mapName) {
        return new InMemoryMap();
    }

    @Override
    public @NotNull Set<String> maps() {
        return this.storeMapRegistry.keySet();
    }

    @Override
    public boolean hasMap(@NotNull String mapName) {
        return this.storeMapRegistry.containsKey(mapName);
    }

    @Override
    public void closeMap(@NotNull String mapName) {
        // nothing to do
    }

    @Override
    public void removeMap(@NotNull String mapName) {
        if (this.storeMapRegistry.containsKey(mapName)) {
            StoreMap storeMap = this.storeMapRegistry.get(mapName);
            storeMap.clear();
            this.storeMapRegistry.remove(mapName);
        }
    }
}
