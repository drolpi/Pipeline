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

package de.natrox.pipeline.document.find;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.condition.Condition;
import de.natrox.pipeline.sort.SortOrder;
import de.natrox.pipeline.sort.SortableFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

final class FindOptionsBuilderImpl implements FindOptions.Builder {

    private SortableFields sortBy;
    private Condition condition;
    private int skip;
    private int limit;

    FindOptionsBuilderImpl() {
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
    public @NotNull FindOptions.Builder sort(@NotNull String field, @NotNull SortOrder sortOrder) {
        Check.notNull(field, "field");
        Check.notNull(sortOrder, "sortOrder");
        if (sortBy != null) {
            sortBy.addField(field, sortOrder);
        } else {
            SortableFields fields = new SortableFields();
            fields.addField(field, sortOrder);
            sortBy = fields;
        }
        return this;
    }

    @Override
    public FindOptions build() {
        return new FindOptionsImpl(skip, limit, condition, sortBy);
    }
}
