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

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.concurrent.LockService;
import de.natrox.pipeline.option.DocumentOptions;
import de.natrox.pipeline.part.connecting.ConnectingStore;
import de.natrox.pipeline.part.store.StoreMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

@ApiStatus.Internal
public final class DocumentRepositoryFactory {

    private final Pipeline pipeline;
    private final ConnectingStore connectingStore;
    private final LockService lockService;
    private final Map<String, DocumentRepository> repositoryMap;
    private final Lock writeLock;

    public DocumentRepositoryFactory(Pipeline pipeline, ConnectingStore connectingStore, LockService lockService) {
        this.pipeline = pipeline;
        this.connectingStore = connectingStore;
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

    public DocumentRepository createRepository(String name, DocumentOptions options) {
        try {
            this.writeLock.lock();
            if (this.repositoryMap.containsKey(name)) {
                DocumentRepository repository = this.repository(name);
                repository.close();
            }

            StoreMap storeMap = this.connectingStore.openMap(name, options);
            DocumentRepository repository = new DocumentRepositoryImpl(name, this.pipeline, this.lockService, this.connectingStore, storeMap, options);
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

    public @NotNull ConnectingStore connectingStore() {
        return this.connectingStore;
    }
}
