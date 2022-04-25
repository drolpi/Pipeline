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

import de.natrox.pipeline.condition.Condition;
import de.natrox.pipeline.repository.Repository;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@ApiStatus.Experimental
public sealed interface DocumentRepository extends Repository<PipeDocument> permits DocumentRepositoryImpl {

    void insert(@NotNull UUID uniqueId, @NotNull PipeDocument document);

    @NotNull Optional<PipeDocument> get(@NotNull UUID uniqueId);

    @NotNull DocumentCursor find(@NotNull Condition condition, @NotNull FindOptions findOptions);

    default DocumentCursor find() {
        //TODO:
        return this.find(null, null);
    }

    default DocumentCursor find(@NotNull Condition condition) {
        //TODO:
        return this.find(condition, null);
    }

    default DocumentCursor find(@NotNull FindOptions findOptions) {
        //TODO:
        return this.find(null, findOptions);
    }

    boolean exists(@NotNull UUID uniqueId);

    void remove(@NotNull UUID uniqueId);

    String name();

}
