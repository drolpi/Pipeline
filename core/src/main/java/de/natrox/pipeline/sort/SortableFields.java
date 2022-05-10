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
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SortableFields implements Comparable<SortableFields>, Serializable {

    private final List<String> fieldNames;
    private final List<Pair<String, SortOrder>> sortingOrders;

    public SortableFields(String field, SortOrder sortOrder) {
        this.sortingOrders = new ArrayList<>();
        this.fieldNames = new ArrayList<>();
        this.and(field, sortOrder);
    }

    public SortableFields and(@NotNull String field, @NotNull SortOrder sortOrder) {
        this.fieldNames.add(field);
        this.sortingOrders.add(Pair.of(field, sortOrder));
        return this;
    }

    public List<Pair<String, SortOrder>> getSortingOrders() {
        return Collections.unmodifiableList(this.sortingOrders);
    }

    public List<String> fieldNames() {
        return Collections.unmodifiableList(this.fieldNames);
    }

    @Override
    public String toString() {
        return this.fieldNames.toString();
    }

    @Override
    public int compareTo(@NotNull SortableFields other) {
        Check.notNull(other, "other");
        int fieldsSize = this.fieldNames().size();
        int otherFieldsSize = other.fieldNames().size();
        int result = Integer.compare(fieldsSize, otherFieldsSize);
        if (result == 0) {
            String[] keys = this.fieldNames().toArray(new String[0]);
            String[] otherKeys = other.fieldNames().toArray(new String[0]);
            for (int i = 0; i < keys.length; i++) {
                int cmp = keys[i].compareTo(otherKeys[i]);
                if (cmp != 0) {
                    return cmp;
                }
            }
        }

        return result;
    }
}
