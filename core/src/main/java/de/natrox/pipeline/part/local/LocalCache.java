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

package de.natrox.pipeline.part.local;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.datatype.instance.InstanceCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public interface LocalCache {

    <S extends PipelineData> @Nullable S get(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID);

    <S extends PipelineData> boolean exists(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID);

    <S extends PipelineData> void save(@NotNull Class<? extends S> dataClass, @NotNull S data);

    <S extends PipelineData> boolean remove(@NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID);

    <S extends PipelineData> @NotNull Set<UUID> keys(@NotNull Class<? extends S> dataClass);

    <S extends PipelineData> @NotNull Set<S> values(@NotNull Class<? extends S> dataClass);

    <S extends PipelineData> @NotNull Map<UUID, S> entries(@NotNull Class<? extends S> dataClass);

    <S extends PipelineData> @NotNull Map<UUID, S> filter(@NotNull Class<? extends S> dataClass, @NotNull BiPredicate<UUID, S> predicate);

    <S extends PipelineData> void iterate(@NotNull Class<? extends S> dataClass, @NotNull BiConsumer<UUID, S> consumer);

    <S extends PipelineData> @NotNull S instantiateData(Pipeline pipeline, @NotNull Class<? extends S> dataClass, @NotNull UUID objectUUID, @Nullable InstanceCreator<S> instanceCreator);
}
