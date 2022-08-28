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

import de.natrox.common.validate.Check;
import de.natrox.eventbus.EventBus;
import de.natrox.eventbus.EventListener;
import de.natrox.pipeline.part.store.StoreMap;
import de.natrox.pipeline.part.updater.Updater;
import de.natrox.pipeline.part.updater.event.EntryRemoveEvent;
import de.natrox.pipeline.part.updater.event.EntryUpdateEvent;
import de.natrox.pipeline.part.updater.event.MapClearEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class PipelineMap implements StoreMap {

    private final String mapName;
    private final StoreMap storageMap;
    private final @Nullable StoreMap globalCacheMap;
    private final @Nullable StoreMap localCacheMap;
    private final @Nullable Updater updater;

    private final DataSynchronizer dataSynchronizer;

    PipelineMap(String mapName, StoreMap storageMap, @Nullable StoreMap globalCacheMap, @Nullable StoreMap localCacheMap, @Nullable Updater updater) {
        this.mapName = mapName;
        this.storageMap = storageMap;
        this.globalCacheMap = globalCacheMap;
        this.localCacheMap = localCacheMap;
        this.updater = updater;
        this.dataSynchronizer = new DataSynchronizer(storageMap, globalCacheMap, localCacheMap);
        this.registerListeners();
    }

    @Override
    public @Nullable Object get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");

        if (this.localCacheMap != null) {
            Object data = this.fromPart(uniqueId, this.localCacheMap);
            if (data != null) {
                return data;
            }
        }

        if (this.globalCacheMap != null) {
            Object data = this.fromPart(uniqueId, this.globalCacheMap, DataSynchronizer.DataSourceType.LOCAL_CACHE);
            if (data != null) {
                return data;
            }
        }

        return this.fromPart(uniqueId, this.storageMap, DataSynchronizer.DataSourceType.LOCAL_CACHE, DataSynchronizer.DataSourceType.GLOBAL_CACHE);
    }

    private Object fromPart(UUID uniqueId, StoreMap storeMap, DataSynchronizer.DataSourceType... destinations) {
        Object data = storeMap.get(uniqueId);
        if (data != null)
            this.dataSynchronizer.synchronizeTo(uniqueId, data, destinations);
        return data;
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull Object data, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(data, "data");
        if (strategies.contains(QueryStrategy.LOCAL_CACHE) || strategies.contains(QueryStrategy.ALL)) {
            if (this.localCacheMap != null) {
                this.localCacheMap.put(uniqueId, data);
            }
            if (this.updater != null) {
                this.updater.pushUpdate(this.mapName, uniqueId, data, () -> {

                });
            }
        }
        if ((strategies.contains(QueryStrategy.GLOBAL_CACHE) || strategies.contains(QueryStrategy.ALL)) && this.globalCacheMap != null) {
            this.globalCacheMap.put(uniqueId, data);
        }

        if (strategies.contains(QueryStrategy.GLOBAL_STORAGE) || strategies.contains(QueryStrategy.ALL)) {
            this.storageMap.put(uniqueId, data);
        }
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        if ((strategies.contains(QueryStrategy.LOCAL_CACHE) || strategies.contains(QueryStrategy.ALL)) && this.localCacheMap != null) {
            boolean localExists = this.localCacheMap.contains(uniqueId, strategies);
            if (localExists)
                return true;
        }

        if ((strategies.contains(QueryStrategy.GLOBAL_CACHE) || strategies.contains(QueryStrategy.ALL)) && this.globalCacheMap != null) {
            boolean globalExists = this.globalCacheMap.contains(uniqueId, strategies);
            if (globalExists)
                return true;
        }

        if (strategies.contains(QueryStrategy.GLOBAL_STORAGE) || strategies.contains(QueryStrategy.ALL)) {
            return this.storageMap.contains(uniqueId, strategies);
        }

        //TODO: message/reason
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Collection<UUID> keys() {
        return this.storageMap.keys();
    }

    @Override
    public @NotNull Collection<Object> values() {
        return this.storageMap.values();
    }

    @Override
    public @NotNull Map<UUID, Object> entries() {
        return this.storageMap.entries();
    }

    @Override
    public void remove(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies) {
        Check.notNull(uniqueId, "uniqueId");
        if (strategies.contains(QueryStrategy.LOCAL_CACHE) || strategies.contains(QueryStrategy.ALL)) {
            if (this.localCacheMap != null) {
                this.localCacheMap.remove(uniqueId, strategies);
            }
            if (this.updater != null) {
                this.updater.pushRemoval(this.mapName, uniqueId, () -> {

                });
            }
        }

        if ((strategies.contains(QueryStrategy.GLOBAL_CACHE) || strategies.contains(QueryStrategy.ALL)) && this.globalCacheMap != null) {
            this.globalCacheMap.remove(uniqueId, strategies);
        }

        if (strategies.contains(QueryStrategy.GLOBAL_STORAGE) || strategies.contains(QueryStrategy.ALL)) {
            this.storageMap.remove(uniqueId, strategies);
        }
    }

    @Override
    public void clear() {
        if (this.localCacheMap != null) {
            this.localCacheMap.clear();
        }
        if (this.updater != null) {
            this.updater.pushClear(this.mapName, () -> {

            });
        }
        if (this.globalCacheMap != null) {
            this.globalCacheMap.clear();
        }
        this.storageMap.clear();
    }

    @Override
    public long size() {
        return this.storageMap.size();
    }

    public void close() {
        this.dataSynchronizer.close();
    }

    private void registerListeners() {
        if (this.localCacheMap == null || this.updater == null)
            return;

        EventBus eventBus = this.updater.eventBus();

        eventBus.register(
            EventListener
                .builder(EntryUpdateEvent.class)
                .condition(event -> event.repositoryName().equals(this.mapName))
                .handler(event -> this.localCacheMap.put(event.dataKey(), event.data()))
                .build()
        );

        eventBus.register(
            EventListener
                .builder(EntryRemoveEvent.class)
                .condition(event -> event.repositoryName().equals(this.mapName))
                .handler(event -> this.localCacheMap.remove(event.dataKey(), Set.of(QueryStrategy.LOCAL_CACHE)))
                .build()
        );

        eventBus.register(
            EventListener
                .builder(MapClearEvent.class)
                .condition(event -> event.repositoryName().equals(this.mapName))
                .handler(event -> this.localCacheMap.clear())
                .build()
        );
    }
}
