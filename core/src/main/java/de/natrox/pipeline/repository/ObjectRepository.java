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

import de.natrox.pipeline.find.FindOptions;
import de.natrox.pipeline.object.InstanceCreator;
import de.natrox.pipeline.object.ObjectData;
import de.natrox.pipeline.stream.Cursor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

@ApiStatus.Experimental
public sealed interface ObjectRepository<T extends ObjectData> extends Repository<T> permits ObjectRepositoryImpl {

    @NotNull Optional<T> load(@NotNull UUID uniqueId, @Nullable InstanceCreator<T> instanceCreator);

    default @NotNull Optional<T> load(@NotNull UUID uniqueId) {
        return this.load(uniqueId, null);
    }

    @NotNull T loadOrCreate(@NotNull UUID uniqueId, @Nullable InstanceCreator<T> instanceCreator);

    default @NotNull T loadOrCreate(@NotNull UUID uniqueId) {
        return this.loadOrCreate(uniqueId, null);
    }

    @NotNull Cursor<T> find(@NotNull FindOptions findOptions, @Nullable InstanceCreator<T> instanceCreator);

    @Override
    default @NotNull Cursor<T> find(@NotNull FindOptions findOptions) {
        return this.find(findOptions, null);
    }

    default @NotNull Cursor<T> find(@NotNull UnaryOperator<FindOptions.Builder> function, @Nullable InstanceCreator<T> instanceCreator) {
        return this.find(function.apply(FindOptions.builder()).build(), instanceCreator);
    }

    default @NotNull Cursor<T> find(@Nullable InstanceCreator<T> instanceCreator) {
        return this.find(FindOptions.defaults(), instanceCreator);
    }

    void save(@NotNull T objectData, QueryStrategy @NotNull ... strategies);

    @NotNull Class<T> type();

    @NotNull DocumentRepository documentRepository();

    sealed interface Builder<T extends ObjectData> extends Repository.Builder<ObjectRepository<T>, Builder<T>> permits ObjectRepositoryImpl.BuilderImpl {

        @NotNull Builder<T> instanceCreator(@NotNull InstanceCreator<T> instanceCreator);

    }

}
