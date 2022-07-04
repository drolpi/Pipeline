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

package de.natrox.pipeline.repository.find;

import de.natrox.pipeline.condition.Condition;
import de.natrox.pipeline.sort.SortEntry;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ClassCanBeRecord")
final class FindOptionsImpl implements FindOptions {

    private final int skip;
    private final int limit;
    private final Condition condition;
    private final SortEntry sortBy;

    FindOptionsImpl(int skip, int limit, Condition condition, SortEntry sortBy) {
        this.skip = skip;
        this.limit = limit;
        this.condition = condition;
        this.sortBy = sortBy;
    }

    public int skip() {
        return this.skip;
    }

    public int limit() {
        return this.limit;
    }

    public @Nullable Condition condition() {
        return this.condition;
    }

    public @Nullable SortEntry sortBy() {
        return this.sortBy;
    }
}
