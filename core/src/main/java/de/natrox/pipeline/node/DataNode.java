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

package de.natrox.pipeline.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DataNode {

    @Nullable Object key();

    @Nullable DataNode parent();

    @NotNull DataNode node(Object @NotNull ... path);

    @NotNull DataNode node(@NotNull Iterable<?> path);

    boolean hasChild(@NotNull Object... path);

    boolean hasChild(@NotNull Iterable<?> path);

    @Nullable Object getAs();

    <T> @Nullable T getAs(@NotNull Class<T> type);

    @NotNull DataNode set(@Nullable Object value);
}
