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

package de.natrox.pipeline.object;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.document.DocumentRepository;
import de.natrox.pipeline.document.DocumentRepositoryFactory;
import de.natrox.pipeline.object.annotation.AnnotationResolver;
import de.natrox.pipeline.object.option.ObjectOptions;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class ObjectRepositoryFactory {

    private final Pipeline pipeline;
    private final DocumentRepositoryFactory documentRepositoryFactory;
    private final Map<String, ObjectRepository<? extends ObjectData>> repositoryMap;

    public ObjectRepositoryFactory(Pipeline pipeline, DocumentRepositoryFactory documentRepositoryFactory) {
        this.pipeline = pipeline;
        this.documentRepositoryFactory = documentRepositoryFactory;
        this.repositoryMap = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends ObjectData> ObjectRepository<T> repository(Class<T> type, ObjectOptions options) {
        String name = AnnotationResolver.identifier(type);

        if (this.repositoryMap.containsKey(name)) {
            ObjectRepository<T> repository = (ObjectRepository<T>) this.repositoryMap.get(name);
            if (!repository.isDropped() && repository.isOpen()) {
                return repository;
            }
            this.repositoryMap.remove(name);
        }
        return this.createRepository(name, type, options);
    }

    private <T extends ObjectData> ObjectRepository<T> createRepository(String name, Class<T> type, ObjectOptions options) {
        DocumentRepository documentRepository = this.documentRepositoryFactory.repository(name, options.toDocumentOptions());
        ObjectRepository<T> repository = new ObjectRepositoryImpl<>(this.pipeline, type, documentRepository, options);
        this.repositoryMap.put(name, repository);

        return repository;
    }

    public void clear() {
        for (ObjectRepository<?> repository : this.repositoryMap.values()) {
            repository.close();
        }
        this.repositoryMap.clear();
    }
}
