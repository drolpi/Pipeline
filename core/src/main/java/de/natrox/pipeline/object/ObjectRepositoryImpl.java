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

import de.natrox.pipeline.document.DocumentCursor;
import de.natrox.pipeline.document.DocumentRepository;
import de.natrox.pipeline.document.PipeDocument;
import de.natrox.pipeline.document.find.FindOptions;
import de.natrox.pipeline.json.JsonConverter;
import de.natrox.pipeline.repository.Cursor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
final class ObjectRepositoryImpl<T extends ObjectData> implements ObjectRepository<T> {

    private final Class<T> type;
    private final DocumentRepository documentRepository;
    private final JsonConverter converter;

    ObjectRepositoryImpl(Class<T> type, DocumentRepository documentRepository, JsonConverter converter) {
        this.type = type;
        this.documentRepository = documentRepository;
        this.converter = converter;
    }

    @Override
    public @NotNull Optional<T> load(@NotNull UUID uniqueId) {
        return documentRepository.get(uniqueId).map(this::convertToObject);
    }

    @Override
    public @NotNull Cursor<T> find(@NotNull FindOptions findOptions) {
        DocumentCursor documentCursor = documentRepository.find(findOptions);
        return new ObjectCursor<>(documentCursor);
    }

    @Override
    public void save(@NotNull ObjectData objectData) {
        documentRepository.insert(objectData.uniqueId(), convertToDocument(objectData));
    }

    @Override
    public boolean exists(@NotNull UUID uniqueId) {
        return documentRepository.exists(uniqueId);
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        documentRepository.remove(uniqueId);
    }

    @Override
    public @NotNull Class<T> type() {
        return type;
    }

    @Override
    public @NotNull DocumentRepository documentRepository() {
        return documentRepository;
    }

    @Override
    public void drop() {

    }

    @Override
    public boolean isDropped() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public void close() {

    }

    private T convertToObject(PipeDocument document) {
        return converter.convert(document, type);
    }

    private PipeDocument convertToDocument(Object object) {
        return converter.convert(object, PipeDocument.class);
    }
}
