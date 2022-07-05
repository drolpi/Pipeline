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

import de.natrox.common.validate.Check;
import de.natrox.eventbus.EventBus;
import de.natrox.eventbus.EventListener;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.mapper.DocumentMapper;
import de.natrox.pipeline.option.Options;
import de.natrox.pipeline.part.store.Store;
import de.natrox.pipeline.part.store.StoreMap;
import de.natrox.pipeline.part.updater.Updater;
import de.natrox.pipeline.part.updater.event.ByteDocumentUpdateEvent;
import de.natrox.pipeline.part.updater.event.DocumentUpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class ConnectingStore implements Store {

    private final Store storage;
    private final @Nullable Store globalCache;
    private final @Nullable Store localCache;
    private final @Nullable Updater updater;

    private final DocumentMapper documentMapper;

    public ConnectingStore(@NotNull Pipeline pipeline, @NotNull Store storage, @Nullable Store globalCache, @Nullable Store localCache, @Nullable Updater updater) {
        this.storage = storage;
        this.globalCache = globalCache;
        this.localCache = localCache;
        this.updater = updater;
        this.documentMapper = pipeline.documentMapper();
        this.registerListeners();
    }

    private void registerListeners() {
        if (this.updater == null)
            return;

        EventBus eventBus = this.updater.eventBus();

        eventBus.register(
            EventListener
                .builder(ByteDocumentUpdateEvent.class)
                .handler(event -> eventBus.call(new DocumentUpdateEvent(
                    event.senderId(),
                    event.repositoryName(),
                    event.documentId(),
                    this.documentMapper.read(event.documentData())
                )))
                .build()
        );
    }

    @Override
    public @NotNull StoreMap openMap(@NotNull String mapName, @NotNull Options options) {
        Check.notNull(mapName, "mapName");
        StoreMap localCacheMap = null;
        Updater updater = null;
        if (options.useLocalCache() && this.localCache != null) {
            localCacheMap = this.localCache.openMap(mapName, options);
            updater = this.updater;
        }

        StoreMap globalCacheMap = null;
        if (options.useGlobalCache() && this.globalCache != null) {
            globalCacheMap = this.globalCache.openMap(mapName, options);
        }

        StoreMap storageMap = this.storage.openMap(mapName, options);

        return new ConnectingMap(mapName, storageMap, globalCacheMap, localCacheMap, this.updater);
    }

    @Override
    public @NotNull Set<String> maps() {
        return this.storage.maps();
    }

    @Override
    public boolean hasMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        //TODO: Maybe check other parts too
        return this.storage.hasMap(mapName);
    }

    @Override
    public void closeMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        if (this.localCache != null) {
            this.localCache.closeMap(mapName);
        }

        if (this.globalCache != null) {
            this.globalCache.closeMap(mapName);
        }

        this.storage.closeMap(mapName);
    }

    @Override
    public void removeMap(@NotNull String mapName) {
        Check.notNull(mapName, "mapName");
        if (this.localCache != null) {
            this.localCache.removeMap(mapName);
        }

        if (this.globalCache != null) {
            this.globalCache.removeMap(mapName);
        }

        this.storage.removeMap(mapName);
    }

    @Override
    public boolean isClosed() {
        return this.storage.isClosed()
            || (this.globalCache != null && this.globalCache.isClosed())
            || (this.localCache != null && this.localCache.isClosed());
    }

    @Override
    public void close() {
        if (this.localCache != null) {
            this.localCache.close();
        }
        if (this.globalCache != null) {
            this.globalCache.close();
        }
        this.storage.close();
    }

    public Store storage() {
        return this.storage;
    }

    public @Nullable Store globalCache() {
        return this.globalCache;
    }

    public @Nullable Store localCache() {
        return this.localCache;
    }

    public @Nullable Updater updater() {
        return this.updater;
    }
}
