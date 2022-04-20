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

package de.natrox.pipeline.operator;

import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.operator.filter.Filter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PipelineStream<T extends PipelineData> {

    @NotNull Optional<T> first();

    @NotNull CompletableFuture<Optional<T>> firstAsync();

    @NotNull List<T> collect();

    @NotNull CompletableFuture<List<T>> collectAsync();

    @NotNull PipelineStream<T> filter(@NotNull Filter filter);

    //TEMP
    @Deprecated
    @NotNull PipelineStream<T> sort(@NotNull Object sorter);

    @NotNull PipelineStream<T> limit(int limit);

    @NotNull PipelineStream<T> skip(int skip);

}
