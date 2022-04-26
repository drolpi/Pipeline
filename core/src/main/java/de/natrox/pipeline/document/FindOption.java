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

public final class FindOption {

    private SortableFields orderBy;
    private Long skip;
    private Long limit;

    FindOption() {

    }

    public FindOption skip(Long skip) {
        this.skip = skip;
        return this;
    }

    public FindOption skip(Integer skip) {
        this.skip = skip == null ? null : (long) skip;
        return this;
    }

    public Long skip() {
        return this.skip;
    }

    public FindOption limit(Long limit) {
        this.limit = limit;
        return this;
    }

    public FindOption limit(Integer limit) {
        this.limit = limit == null ? null : (long) limit;
        return this;
    }

    public Long limit() {
        return this.limit;
    }

    public FindOption orderBy(String fieldName, SortOrder sortOrder) {
        if (orderBy != null) {
            orderBy.addField(fieldName, sortOrder);
        } else {
            SortableFields fields = new SortableFields();
            fields.addField(fieldName, sortOrder);
            orderBy = fields;
        }
        return this;
    }

    public SortableFields orderBy() {
        return this.orderBy;
    }
}
