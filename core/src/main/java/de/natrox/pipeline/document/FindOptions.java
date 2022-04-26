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

import de.natrox.common.validate.Check;
import de.natrox.pipeline.sort.SortOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class FindOptions {

    private FindOptions() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull FindOption orderBy(@NotNull String field, @NotNull SortOrder sortOrder) {
        Check.notNull(field, "field");
        Check.notNull(sortOrder, "sortOrder");

        FindOption findOption = new FindOption();
        findOption.orderBy(field, sortOrder);
        return findOption;
    }

    public static @NotNull FindOption skip(@Range(from = 0, to = Integer.MAX_VALUE) int skip) {
        Check.argCondition(skip < 0, "skip");

        FindOption findOption = new FindOption();
        findOption.skip(skip);
        return findOption;
    }

    public static @NotNull FindOption limit(@Range(from = 0, to = Integer.MAX_VALUE) int limit) {
        Check.argCondition(limit < 0, "limit");

        FindOption findOption = new FindOption();
        findOption.limit(limit);
        return findOption;
    }
}
