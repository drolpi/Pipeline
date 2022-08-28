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

package de.natrox.pipeline.sort;

import de.natrox.common.container.Pair;
import de.natrox.pipeline.node.DataNode;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public final class DocumentSorter implements Comparator<Pair<UUID, DataNode>> {

    private final List<Pair<Object[], SortOrder>> sortOrder;

    public DocumentSorter(List<Pair<Object[], SortOrder>> sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public int compare(Pair<UUID, DataNode> pair1, Pair<UUID, DataNode> pair2) {
        if (this.sortOrder == null || this.sortOrder.isEmpty())
            return 0;

        for (Pair<Object[], SortOrder> pair : this.sortOrder) {
            DataNode doc1 = pair1.second();
            DataNode doc2 = pair2.second();

            Object value1 = doc1.node(pair.first()).getAs();
            Object value2 = doc2.node(pair.first()).getAs();

            int result;
            if (value1 == null && value2 != null) {
                result = -1;
            } else if (value1 != null && value2 == null) {
                result = 1;
            } else if (value1 == null) {
                result = -1;
            } else {

                if (value1.getClass().isArray()
                    || value1 instanceof Iterable
                    || value2.getClass().isArray()
                    || value2 instanceof Iterable) {
                    throw new RuntimeException("Cannot sort on an array or collection object");
                }

                Comparable c1 = (Comparable) value1;
                Comparable c2 = (Comparable) value2;
                result = c1.compareTo(c2);
            }

            if (pair.second() == SortOrder.Descending) {
                result *= -1;
            }

            if (result != 0) {
                return result;
            }
        }

        return 0;
    }
}
