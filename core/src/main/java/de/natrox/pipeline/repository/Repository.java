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

import de.natrox.common.builder.IBuilder;
import de.natrox.common.function.SingleTypeFunction;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.find.FindOptions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@ApiStatus.Experimental
public sealed interface Repository<T> permits DocumentRepository, ObjectRepository {

    @NotNull Cursor<T> find(@NotNull FindOptions findOptions);

    default @NotNull Cursor<T> find(@NotNull SingleTypeFunction<FindOptions.@NotNull Builder> function) {
        Check.notNull(function, "function");
        return this.find(function.apply(FindOptions.builder()).build());
    }

    default Cursor<T> find() {
        return this.find(FindOptions.defaults());
    }

    boolean exists(@NotNull UUID uniqueId);

    void remove(@NotNull UUID uniqueId);

    void clear();

    void drop();

    boolean isDropped();

    void close();

    boolean isOpen();

    long size();

    sealed interface Builder<T extends Repository<?>, R extends Builder<T, R>> extends IBuilder<T> permits AbstractRepositoryBuilder, DocumentRepository.Builder, ObjectRepository.Builder {

        @NotNull R useGlobalCache(boolean use);

        @NotNull R useLocalCache(boolean use);

    }
}
