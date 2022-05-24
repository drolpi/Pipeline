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

package de.natrox.pipeline.part.connecting;

import de.natrox.common.container.Pair;
import de.natrox.common.validate.Check;
import de.natrox.eventbus.EventBus;
import de.natrox.eventbus.EventListener;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.part.StoreMap;
import de.natrox.pipeline.part.updater.event.DocumentUpdateEvent;
import de.natrox.pipeline.part.updater.Updater;
import de.natrox.pipeline.stream.PipeStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ConnectingMap implements StoreMap {

    private final String mapName;
    private final StoreMap storageMap;
    private final @Nullable StoreMap globalCacheMap;
    private final @Nullable StoreMap localCacheMap;
    private final @Nullable Updater updater;

    private final DataSynchronizer dataSynchronizer;

    public ConnectingMap(String mapName, StoreMap storageMap, @Nullable StoreMap globalCacheMap, @Nullable StoreMap localCacheMap, @Nullable Updater updater) {
        this.mapName = mapName;
        this.storageMap = storageMap;
        this.globalCacheMap = globalCacheMap;
        this.localCacheMap = localCacheMap;
        this.updater = updater;
        this.dataSynchronizer = new DataSynchronizer(this);
        this.registerListeners();
    }

    private void registerListeners() {
        if (this.localCacheMap == null || this.updater == null)
            return;

        EventBus eventBus = this.updater.eventBus();

        eventBus.register(
            EventListener
                .builder(DocumentUpdateEvent.class)
                .condition(event -> event.repositoryName().equals(this.mapName))
                .handler(event -> this.localCacheMap.put(event.documentId(), event.documentData()))
                .build()
        );

        eventBus.register(
            EventListener
                .builder(DocumentUpdateEvent.class)
                .condition(event -> event.repositoryName().equals(this.mapName))
                .handler(event -> this.localCacheMap.remove(event.documentId()))
                .build()
        );

        eventBus.register(
            EventListener
                .builder(DocumentUpdateEvent.class)
                .condition(event -> event.repositoryName().equals(this.mapName))
                .handler(event -> this.localCacheMap.clear())
                .build()
        );
    }

    @Override
    public @Nullable DocumentData get(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");

        if (this.localCacheMap != null) {
            DocumentData documentData = this.fromPart(uniqueId, this.localCacheMap);
            if (documentData != null) {
                return documentData;
            }
        }

        if (this.globalCacheMap != null) {
            DocumentData documentData = this.fromPart(uniqueId, this.globalCacheMap, DataSynchronizer.DataSourceType.LOCAL_CACHE);
            if (documentData != null) {
                return documentData;
            }
        }

        return this.fromPart(uniqueId, this.storageMap, DataSynchronizer.DataSourceType.LOCAL_CACHE, DataSynchronizer.DataSourceType.GLOBAL_CACHE);
    }

    private DocumentData fromPart(UUID uniqueId, StoreMap storeMap, DataSynchronizer.DataSourceType... destinations) {
        DocumentData documentData = storeMap.get(uniqueId);
        if(documentData != null)
            this.dataSynchronizer.synchronizeTo(uniqueId, documentData, destinations);
        return documentData;
    }

    @Override
    public void put(@NotNull UUID uniqueId, @NotNull DocumentData documentData) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(documentData, "documentData");
        if (this.localCacheMap != null && this.updater != null) {
            this.localCacheMap.put(uniqueId, documentData);
            this.updater.pushUpdate(this.mapName, uniqueId, documentData, () -> {

            });
        }
        if (this.globalCacheMap != null) {
            this.globalCacheMap.put(uniqueId, documentData);
        }
        this.storageMap.put(uniqueId, documentData);
    }

    @Override
    public boolean contains(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        if (this.localCacheMap != null) {
            boolean localExists = this.localCacheMap.contains(uniqueId);
            if (localExists)
                return true;
        }

        if (this.globalCacheMap != null) {
            boolean globalExists = this.globalCacheMap.contains(uniqueId);
            if (globalExists)
                return true;
        }

        return this.storageMap.contains(uniqueId);
    }

    @Override
    public @NotNull PipeStream<UUID> keys() {
        return this.storageMap.keys();
    }

    @Override
    public @NotNull PipeStream<DocumentData> values() {
        return this.storageMap.values();
    }

    @Override
    public @NotNull PipeStream<Pair<UUID, DocumentData>> entries() {
        return this.storageMap.entries();
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        if (this.localCacheMap != null && this.updater != null) {
            this.localCacheMap.remove(uniqueId);
            this.updater.pushRemoval(this.mapName, uniqueId, () -> {

            });
        }
        if (this.globalCacheMap != null) {
            this.globalCacheMap.remove(uniqueId);
        }
        this.storageMap.remove(uniqueId);
    }

    @Override
    public void clear() {
        if (this.localCacheMap != null && this.updater != null) {
            this.localCacheMap.clear();
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

    public StoreMap storageMap() {
        return this.storageMap;
    }

    public StoreMap globalCacheMap() {
        return this.globalCacheMap;
    }

    public StoreMap localCacheMap() {
        return this.localCacheMap;
    }
}
