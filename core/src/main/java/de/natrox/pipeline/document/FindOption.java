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

package de.natrox.pipeline.document;

import de.natrox.pipeline.sort.SortOrder;
import de.natrox.pipeline.sort.SortableFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class FindOption {

    private SortableFields orderBy;
    private int skip;
    private int limit;

    FindOption() {

    }

    public @NotNull FindOption skip(@Range(from = 0, to = Integer.MAX_VALUE) int skip) {
        this.skip = skip;
        return this;
    }

    public int skip() {
        return this.skip;
    }

    public @NotNull FindOption limit(@Range(from = 0, to = Integer.MAX_VALUE) int limit) {
        this.limit = limit;
        return this;
    }

    public int limit() {
        return this.limit;
    }

    public @NotNull FindOption orderBy(@NotNull String fieldName, @NotNull SortOrder sortOrder) {
        if (orderBy != null) {
            orderBy.addField(fieldName, sortOrder);
        } else {
            SortableFields fields = new SortableFields();
            fields.addField(fieldName, sortOrder);
            orderBy = fields;
        }
        return this;
    }

    public @NotNull SortableFields orderBy() {
        return this.orderBy;
    }
}
