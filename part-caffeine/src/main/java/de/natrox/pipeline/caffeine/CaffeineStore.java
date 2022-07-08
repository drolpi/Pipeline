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

import de.natrox.common.validate.Check;
import de.natrox.pipeline.part.store.AbstractStore;
import de.natrox.pipeline.part.store.StoreMap;
import de.natrox.pipeline.repository.RepositoryOptions;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

final class CaffeineStore extends AbstractStore {

    CaffeineStore() {

    }

    @Override
    protected StoreMap createMap(@NotNull String mapName, @NotNull RepositoryOptions options) {
        Check.notNull(mapName, "mapName");
        return new CaffeineMap(options);
    }

    @Override
    public @NotNull Set<String> maps() {
        return this.storeMapRegistry.keySet();
    }

    @Override
    public boolean hasMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        return this.storeMapRegistry.containsKey(mapName);
    }

    @Override
    public void closeMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        // nothing to do
    }

    @Override
    public void removeMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        if (this.storeMapRegistry.containsKey(mapName)) {
            StoreMap storeMap = this.storeMapRegistry.get(mapName);
            storeMap.clear();
            this.storeMapRegistry.remove(mapName);
        }
    }

}
