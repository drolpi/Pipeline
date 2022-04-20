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

package de.natrox.pipeline.part;

import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.json.document.JsonDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public interface DataProvider {

    @Nullable JsonDocument get(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID);

    boolean exists(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID);

    void save(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID, @NotNull JsonDocument dataToSave);

    boolean remove(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID);

    @NotNull Collection<UUID> keys(@NotNull Class<? extends PipelineData> dataClass);

    @NotNull Collection<JsonDocument> documents(@NotNull Class<? extends PipelineData> dataClass);

    @NotNull Map<UUID, JsonDocument> entries(@NotNull Class<? extends PipelineData> dataClass);

    @NotNull Map<UUID, JsonDocument> filter(@NotNull Class<? extends PipelineData> dataClass, @NotNull BiPredicate<UUID, JsonDocument> predicate);

    void iterate(@NotNull Class<? extends PipelineData> dataClass, @NotNull BiConsumer<UUID, JsonDocument> consumer);

}
