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
import de.natrox.pipeline.concurrent.LockService;
import de.natrox.pipeline.exception.PipelineException;
import de.natrox.pipeline.mapper.DocumentMapper;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.object.annotation.AnnotationResolver;
import de.natrox.pipeline.part.store.Store;
import de.natrox.pipeline.part.updater.Updater;
import de.natrox.pipeline.part.updater.event.ByteDocumentUpdateEvent;
import de.natrox.pipeline.part.updater.event.DocumentUpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

final class PipelineImpl implements Pipeline {

    private final Store storage;
    private final @Nullable Store globalCache;
    private final @Nullable Store localCache;
    private final @Nullable Updater updater;

    private final DocumentMapper documentMapper;
    private final DocumentRepositoryFactory documentRepositoryFactory;
    private final ObjectRepositoryFactory objectRepositoryFactory;

    PipelineImpl(@NotNull Store storage, @Nullable Store globalCache, @Nullable Store localCache, @Nullable Updater updater) {
        Check.notNull(storage, "storage");
        this.storage = storage;
        this.globalCache = globalCache;
        this.localCache = localCache;
        this.updater = updater;

        this.documentMapper = DocumentMapper.create();

        LockService lockService = new LockService();
        this.documentRepositoryFactory = new DocumentRepositoryFactory(this, lockService);
        this.objectRepositoryFactory = new ObjectRepositoryFactory(this, this.documentRepositoryFactory);

        this.registerListeners();
    }

    @Override
    public @NotNull DocumentRepository repository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        return this.documentRepositoryFactory.repository(name);
    }

    @Override
    public @NotNull DocumentRepository.Builder buildRepository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        return new DocumentRepositoryImpl.BuilderImpl(this.documentRepositoryFactory, name);
    }

    @Override
    public @NotNull <T extends ObjectData> ObjectRepository<T> repository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        this.checkOpened();
        return this.objectRepositoryFactory.repository(type);
    }

    @Override
    public <T extends ObjectData> ObjectRepository.@NotNull Builder<T> buildRepository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        this.checkOpened();
        return new ObjectRepositoryImpl.BuilderImpl<>(this.objectRepositoryFactory, type);
    }

    @Override
    public boolean hasRepository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        //TODO: Maybe check other parts too
        return this.storage.hasMap(name);
    }

    @Override
    public <T> boolean hasRepository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        this.checkOpened();
        //TODO: Maybe check other parts too
        return this.storage.hasMap(AnnotationResolver.identifier(type));
    }

    @Override
    public void destroyRepository(@NotNull String name) {
        Check.notNull(name, "name");
        this.checkOpened();
        this.removeMap(name);
    }

    @Override
    public <T extends ObjectData> void destroyRepository(@NotNull Class<T> type) {
        Check.notNull(type, "type");
        this.checkOpened();
        this.removeMap(AnnotationResolver.identifier(type));
    }

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
    public @NotNull Set<String> repositories() {
        this.checkOpened();
        return this.storage.maps();
    }

    @Override
    public @NotNull DocumentMapper documentMapper() {
        return this.documentMapper;
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
        this.documentRepositoryFactory.clear();
        this.objectRepositoryFactory.clear();
    }

    private void checkOpened() {
        if (!this.isClosed())
            return;
        throw new PipelineException("Pipeline is closed");
    }

    private void registerListeners() {
        if (this.updater == null)
            return;

        EventBus eventBus = this.updater.eventBus();

        eventBus.register(ByteDocumentUpdateEvent.class, event -> eventBus.call(new DocumentUpdateEvent(
                event.senderId(),
                event.repositoryName(),
                event.documentId(),
                this.documentMapper.read(event.documentData())
            ))
        );
    }

    public @NotNull Store storage() {
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
