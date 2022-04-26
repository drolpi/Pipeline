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

import de.natrox.pipeline.condition.Conditions;
import de.natrox.pipeline.document.DocumentRepository;
import de.natrox.pipeline.document.FindOptions;
import de.natrox.pipeline.condition.Condition;
import de.natrox.pipeline.repository.Cursor;
import de.natrox.pipeline.repository.Repository;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@ApiStatus.Experimental
public sealed interface ObjectRepository<T extends ObjectData> extends Repository<T> permits ObjectRepositoryImpl {

    @NotNull Optional<T> load(@NotNull UUID uniqueId);

    @NotNull Cursor<T> find(@NotNull Condition condition, @NotNull FindOptions findOptions);

    default Cursor<T> find() {
        //TODO:
        return this.find(Conditions.ALL, null);
    }

    default Cursor<T> find(@NotNull Condition condition) {
        //TODO:
        return this.find(condition, null);
    }

    default Cursor<T> find(@NotNull FindOptions findOptions) {
        return this.find(Conditions.ALL, findOptions);
    }

    void save(@NotNull ObjectData objectData);

    boolean exists(@NotNull UUID uniqueId);

    void remove(@NotNull UUID uniqueId);

    @NotNull Class<T> type();

    @NotNull DocumentRepository documentRepository();

}
