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

import java.util.Arrays;
import java.util.List;

final class SortEntryImpl implements SortEntry {

    private final List<Pair<Object[], SortOrder>> sortingOrders;

    SortEntryImpl(List<Pair<Object[], SortOrder>> orders) {
        this.sortingOrders = orders;
    }

    @SafeVarargs
    SortEntryImpl(Pair<Object[], SortOrder>... orders) {
        this(Arrays.asList(orders));
    }

    @Override
    public List<Pair<Object[], SortOrder>> sortingOrders() {
        return this.sortingOrders;
    }
}
