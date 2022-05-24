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

package de.natrox.pipeline.part;

import de.natrox.common.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractStore implements Store {

    protected final Map<String, StoreMap> storeMapRegistry = new ConcurrentHashMap<>();
    protected volatile boolean closed;

    protected abstract StoreMap createMap(@NotNull String mapName);

    @Override
    public @NotNull StoreMap openMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        if (this.storeMapRegistry.containsKey(mapName)) {
            return this.storeMapRegistry.get(mapName);
        }
        StoreMap storeMap = this.createMap(mapName);
        this.storeMapRegistry.put(mapName, storeMap);

        return storeMap;
    }

    @Override
    public void closeMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        this.storeMapRegistry.remove(mapName);
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        this.closed = true;

        this.storeMapRegistry.clear();
    }

}
