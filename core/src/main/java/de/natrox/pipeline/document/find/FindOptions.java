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

package de.natrox.pipeline.document.find;

import de.natrox.common.builder.IBuilder;
import de.natrox.pipeline.condition.Condition;
import de.natrox.pipeline.sort.SortEntry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@ApiStatus.Experimental
public sealed interface FindOptions permits FindOptionsImpl {

    @NotNull FindOptions DEFAULT = FindOptions.builder().build();

    static @NotNull Builder builder() {
        return new FindOptionsBuilderImpl();
    }

    int skip();

    int limit();

    @Nullable Condition condition();

    @Nullable SortEntry sortBy();

    @ApiStatus.Experimental
    sealed interface Builder extends IBuilder<FindOptions> permits FindOptionsBuilderImpl {

        @NotNull Builder skip(@Range(from = 0, to = Integer.MAX_VALUE) int skip);

        @NotNull Builder limit(@Range(from = 0, to = Integer.MAX_VALUE) int limit);

        @NotNull Builder condition(@NotNull Condition condition);

        @NotNull Builder sort(@NotNull SortEntry sortableFields);

    }

}
