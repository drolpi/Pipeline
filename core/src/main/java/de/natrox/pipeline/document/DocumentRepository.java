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

import de.natrox.pipeline.filter.Filter;
import de.natrox.pipeline.repository.Repository;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public sealed interface DocumentRepository extends Repository<Document> permits DocumentRepositoryImpl {

    @NotNull Optional<Document> get(@NotNull UUID uniqueId);

    @NotNull DocumentCursor find(@NotNull Filter filter, @NotNull FindOptions findOptions);

    default DocumentCursor find() {
        //TODO:
        return this.find(null, null);
    }

    default DocumentCursor find(@NotNull Filter filter) {
        //TODO:
        return this.find(filter, null);
    }

    default DocumentCursor find(@NotNull FindOptions findOptions) {
        //TODO:
        return this.find(null, findOptions);
    }

    void insert(@NotNull UUID uniqueId, @NotNull Document document);

    default void update(@NotNull UUID uniqueId, @NotNull Document document) {
        this.update(uniqueId, document);
    }

    void update(@NotNull UUID uniqueId, @NotNull Document document, boolean insertIfAbsent);

    boolean exists(@NotNull UUID uniqueId);

    void remove(@NotNull UUID uniqueId);

    String name();

}
