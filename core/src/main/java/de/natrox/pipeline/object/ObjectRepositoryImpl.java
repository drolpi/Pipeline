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

import de.natrox.common.validate.Check;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.document.DocumentData;
import de.natrox.pipeline.document.DocumentRepository;
import de.natrox.pipeline.document.find.FindOptions;
import de.natrox.pipeline.object.option.ObjectOptions;
import de.natrox.pipeline.repository.Cursor;
import de.natrox.pipeline.stream.DocumentStream;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

final class ObjectRepositoryImpl<T extends ObjectData> implements ObjectRepository<T> {

    private final Class<T> type;
    private final DocumentRepository documentRepository;
    private final ObjectCache<T> objectCache;

    ObjectRepositoryImpl(Pipeline pipeline, Class<T> type, DocumentRepository documentRepository, ObjectOptions<T> options) {
        this.type = type;
        this.documentRepository = documentRepository;
        this.objectCache = new ObjectCache<>(pipeline, type, options);
    }

    @Override
    public @NotNull Optional<T> load(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        return this.documentRepository.get(uniqueId).map(document -> this.convertToData(uniqueId, document));
    }

    @Override
    public @NotNull T loadOrCreate(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        return this.load(uniqueId).orElseGet(() -> this.create(uniqueId));
    }

    private T create(UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        T data = this.objectCache.get(uniqueId);
        DocumentData documentData = this.convertToDocument(data);
        this.documentRepository.insert(uniqueId, documentData);
        return data;
    }

    @Override
    public @NotNull Cursor<T> find(@NotNull FindOptions findOptions) {
        Check.notNull(findOptions, "findOptions");
        Cursor<DocumentData> documentCursor = this.documentRepository.find(findOptions);
        return new ObjectCursor<>(this, ((DocumentStream) documentCursor).asPairStream());
    }

    @Override
    public void save(@NotNull T objectData) {
        Check.notNull(objectData, "objectData");
        this.documentRepository.insert(objectData.uniqueId(), this.convertToDocument(objectData));
    }

    @Override
    public boolean exists(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        return this.documentRepository.exists(uniqueId);
    }

    @Override
    public void remove(@NotNull UUID uniqueId) {
        Check.notNull(uniqueId, "uniqueId");
        this.documentRepository.remove(uniqueId);
    }

    @Override
    public @NotNull Class<T> type() {
        return this.type;
    }

    @Override
    public @NotNull DocumentRepository documentRepository() {
        return this.documentRepository;
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

    public T convertToData(UUID uniqueId, DocumentData document) {
        T data = this.objectCache.get(uniqueId);
        data.deserialize(document);
        return data;
    }

    private DocumentData convertToDocument(T data) {
        return data.serialize();
    }
}
