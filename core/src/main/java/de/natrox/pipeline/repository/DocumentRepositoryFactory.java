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
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

@ApiStatus.Internal
final class DocumentRepositoryFactory {

    private final PipelineImpl pipeline;
    private final PipelineStore pipelineStore;

    private final LockService lockService;
    private final Map<String, DocumentRepositoryImpl> repositoryMap;
    private final Lock writeLock;

    DocumentRepositoryFactory(PipelineImpl pipeline, PipelineStore pipelineStore, LockService lockService) {
        this.pipeline = pipeline;
        this.pipelineStore = pipelineStore;
        this.lockService = lockService;
        this.writeLock = lockService.getWriteLock(this.getClass());
        this.repositoryMap = new HashMap<>();
    }

    public DocumentRepositoryImpl repository(String name) {
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

    public DocumentRepositoryImpl createRepository(String name, RepositoryOptions.DocumentOptions options) {
        try {
            this.writeLock.lock();
            if (this.repositoryMap.containsKey(name)) {
                DocumentRepositoryImpl repository = this.repository(name);
                repository.close();
            }

            final PipelineMap pipelineMap = this.pipelineStore.openMap(name, options);
            DocumentRepositoryImpl repository = new DocumentRepositoryImpl(name, this.pipeline, this.pipelineStore, pipelineMap, this.lockService, options);
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
