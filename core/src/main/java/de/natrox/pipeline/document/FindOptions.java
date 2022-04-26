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

public final class FindOptions {

    private FindOptions() {
        throw new UnsupportedOperationException();
    }

    public static FindOption orderBy(String fieldName, SortOrder sortOrder) {
        FindOption findOption = new FindOption();
        findOption.orderBy(fieldName, sortOrder);
        return findOption;
    }

    public static FindOption skip(long skip) {
        FindOption findOption = new FindOption();
        findOption.skip(skip);
        return findOption;
    }

    public static FindOption limit(long limit) {
        FindOption findOption = new FindOption();
        findOption.limit(limit);
        return findOption;
    }
}
