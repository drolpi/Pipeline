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
import de.natrox.common.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

@ApiStatus.Experimental
public final class Sorts {

    private Sorts() {
        throw new UnsupportedOperationException();
    }

    public static SortEntry and(@NotNull SortEntry... sortEntries) {
        Check.notNull(sortEntries, "sortEntries");
        Check.argCondition(sortEntries.length < 2, "At least two sort entries must be specified");
        return new SortEntryImpl(Arrays.stream(sortEntries).flatMap(sortEntry -> sortEntry.sortingOrders().stream()).collect(Collectors.toList()));
    }

    public static SortEntry ascending(@NotNull String field) {
        Check.notNull(field, "field");
        return new SortEntryImpl(new Pair<>(field, SortOrder.Ascending));
    }

    public static SortEntry descending(@NotNull String field) {
        Check.notNull(field, "field");
        return new SortEntryImpl(new Pair<>(field, SortOrder.Descending));
    }

}
