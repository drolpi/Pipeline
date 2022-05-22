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

package de.natrox.pipeline.document;

import de.natrox.pipeline.document.option.DocumentOptions;
import de.natrox.pipeline.part.StoreMap;
import de.natrox.pipeline.part.connecting.ConnectingStore;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class DocumentRepositoryFactory {

    private final ConnectingStore connectingStore;
    private final Map<String, DocumentRepository> repositoryMap;

    public DocumentRepositoryFactory(ConnectingStore connectingStore) {
        this.connectingStore = connectingStore;
        this.repositoryMap = new HashMap<>();
    }

    public DocumentRepository repository(String name, DocumentOptions options) {
        if (this.repositoryMap.containsKey(name)) {
            DocumentRepository repository = this.repositoryMap.get(name);
            if (!repository.isDropped() && repository.isOpen()) {
                return this.repositoryMap.get(name);
            }
            this.repositoryMap.remove(name);
        }
        return this.createRepository(name, options);
    }

    private DocumentRepository createRepository(String name, DocumentOptions options) {
        StoreMap storeMap = this.connectingStore.openMap(name);
        DocumentRepository repository = new DocumentRepositoryImpl(name, this.connectingStore, storeMap, options);
        this.repositoryMap.put(name, repository);

        return repository;
    }

    public void clear() {
        for (DocumentRepository collection : this.repositoryMap.values()) {
            collection.close();
        }
        this.repositoryMap.clear();
    }
}
