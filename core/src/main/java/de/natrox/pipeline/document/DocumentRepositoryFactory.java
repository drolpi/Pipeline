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

import de.natrox.pipeline.document.DocumentRepository;
import de.natrox.pipeline.document.DocumentRepositoryImpl;
import de.natrox.pipeline.part.connecting.ConnectingPart;
import de.natrox.pipeline.part.map.PartMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class DocumentRepositoryFactory {

    private final Map<String, DocumentRepository> repositoryMap;

    public DocumentRepositoryFactory() {
        this.repositoryMap = new HashMap<>();
    }

    public DocumentRepository repository(String name, ConnectingPart connectingPart) {
        if (repositoryMap.containsKey(name)) {
            DocumentRepository repository = repositoryMap.get(name);
            if (repository.isDropped() || !repository.isOpen()) {
                repositoryMap.remove(name);
                return createRepository(name, connectingPart);
            }
            return repositoryMap.get(name);
        } else {
            return createRepository(name, connectingPart);
        }
    }

    private DocumentRepository createRepository(String name, ConnectingPart connectingPart) {
        PartMap partMap = connectingPart.openMap(name);
        DocumentRepository repository = new DocumentRepositoryImpl(name, partMap);

        return repository;
    }

    public void clear() {
        for (DocumentRepository collection : repositoryMap.values()) {
            collection.close();
        }
        repositoryMap.clear();
    }
}
