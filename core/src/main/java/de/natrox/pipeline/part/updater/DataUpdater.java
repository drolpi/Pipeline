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

package de.natrox.pipeline.part.updater;

import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface DataUpdater {

    /**
     * Pushes the local data to Pipeline
     */
    void pushUpdate(@NotNull PipelineData pipelineData, @Nullable Runnable callback);

    /**
     * Notifies other Servers that hold this data to delete it from local Cache
     */
    void pushRemoval(@NotNull PipelineData pipelineData, @Nullable Runnable callback);

    @NotNull Optional<PipelineData> applySync(@NotNull PipelineData pipelineData);
}
