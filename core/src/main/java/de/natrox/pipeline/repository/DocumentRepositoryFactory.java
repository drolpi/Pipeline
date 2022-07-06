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

import de.natrox.pipeline.concurrent.LockService;
import de.natrox.pipeline.part.store.Store;
import de.natrox.pipeline.part.store.StoreMap;
import de.natrox.pipeline.part.updater.Updater;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

@ApiStatus.Internal
final class DocumentRepositoryFactory {

    private final PipelineImpl pipeline;
    private final Store storage;
    private final @Nullable Store globalCache;
    private final @Nullable Store localCache;
    private final @Nullable Updater updater;

    private final LockService lockService;
    private final Map<String, DocumentRepository> repositoryMap;
    private final Lock writeLock;

    DocumentRepositoryFactory(PipelineImpl pipeline, LockService lockService) {
        this.pipeline = pipeline;
        this.storage = pipeline.storage();
        this.globalCache = pipeline.globalCache();
        this.localCache = pipeline.localCache();
        this.updater = pipeline.updater();
        this.lockService = lockService;
        this.writeLock = lockService.getWriteLock(this.getClass());
        this.repositoryMap = new HashMap<>();
    }

    public DocumentRepository repository(String name) {
        try {
            this.writeLock.lock();
            if (this.repositoryMap.containsKey(name)) {
                DocumentRepository repository = this.repositoryMap.get(name);
                if (!repository.isDropped() && repository.isOpen()) {
                    return this.repositoryMap.get(name);
                }
                this.repositoryMap.remove(name);
            }
        } finally {
            this.writeLock.unlock();
        }

        //TODO: message/reason
        throw new IllegalStateException();
    }

    public DocumentRepository createRepository(String name, RepositoryOptions.DocumentOptions options) {
        try {
            this.writeLock.lock();
            if (this.repositoryMap.containsKey(name)) {
                DocumentRepository repository = this.repository(name);
                repository.close();
            }

            StoreMap localCacheMap = null;
            Updater updater = null;
            if (options.useLocalCache() && this.localCache != null) {
                localCacheMap = this.localCache.openMap(name, options);
                updater = this.updater;
            }

            StoreMap globalCacheMap = null;
            if (options.useGlobalCache() && this.globalCache != null) {
                globalCacheMap = this.globalCache.openMap(name, options);
            }

            final StoreMap storageMap = this.storage.openMap(name, options);
            final PipelineMap pipelineMap = new PipelineMap(name, storageMap, globalCacheMap, localCacheMap, updater);
            DocumentRepository repository = new DocumentRepositoryImpl(name, this.pipeline, pipelineMap, this.lockService, options);
            this.repositoryMap.put(name, repository);

            return repository;
        } finally {
            this.writeLock.unlock();
        }
    }

    public void clear() {
        try {
            this.writeLock.lock();
            for (DocumentRepository collection : this.repositoryMap.values()) {
                collection.close();
            }
            this.repositoryMap.clear();
        } finally {
            this.writeLock.unlock();
        }
    }
}
