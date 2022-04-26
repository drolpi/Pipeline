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

package de.natrox.pipeline.sort;

import de.natrox.common.container.Pair;
import de.natrox.common.validate.Check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SortableFields extends Fields {

    private final List<Pair<String, SortOrder>> sortingOrders;

    public SortableFields() {
        super();
        sortingOrders = new ArrayList<>();
    }

    public static SortableFields withNames(String... fields) {
        Check.notNull(fields, "fields cannot be null");

        SortableFields sortableFields = new SortableFields();
        for (String field : fields) {
            sortableFields.addField(field, SortOrder.Ascending);
        }
        return sortableFields;
    }

    public SortableFields addField(String field, SortOrder sortOrder) {
        super.fieldNames.add(field);
        this.sortingOrders.add(Pair.of(field, sortOrder));
        return this;
    }

    public List<Pair<String, SortOrder>> getSortingOrders() {
        return Collections.unmodifiableList(sortingOrders);
    }
}
