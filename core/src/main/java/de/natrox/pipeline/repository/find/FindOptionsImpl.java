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

import de.natrox.common.validate.Check;
import de.natrox.pipeline.condition.Condition;
import de.natrox.pipeline.sort.SortEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@SuppressWarnings("ClassCanBeRecord")
final class FindOptionsImpl implements FindOptions {

    final static FindOptions DEFAULT = FindOptions.builder().build();

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

    final static class BuilderImpl implements FindOptions.Builder {

        private SortEntry sortBy;
        private Condition condition;
        private int skip;
        private int limit;

        BuilderImpl() {
            this.skip = -1;
            this.limit = -1;
        }

        @Override
        public @NotNull FindOptions.Builder skip(@Range(from = 0, to = Integer.MAX_VALUE) int skip) {
            Check.argCondition(skip < 0, "skip");
            this.skip = skip;
            return this;
        }

        @Override
        public @NotNull FindOptions.Builder limit(@Range(from = 0, to = Integer.MAX_VALUE) int limit) {
            Check.argCondition(limit < 0, "limit");
            this.limit = limit;
            return this;
        }

        @Override
        public FindOptions.@NotNull Builder condition(@NotNull Condition condition) {
            Check.notNull(condition, "condition");
            this.condition = condition;
            return this;
        }

        @Override
        public FindOptions.@NotNull Builder sort(@NotNull SortEntry sortBy) {
            Check.notNull(sortBy, "sortBy");
            this.sortBy = sortBy;
            return this;
        }

        @Override
        public FindOptions build() {
            return new FindOptionsImpl(this.skip, this.limit, this.condition, this.sortBy);
        }
    }
}
