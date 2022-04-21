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

package de.natrox.pipeline.operator.filter;

import de.natrox.common.validate.Check;
import org.jetbrains.annotations.NotNull;

public final class Filters {

    private Filters() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public static Filter or(@NotNull Filter first, @NotNull Filter second) {
        Check.notNull(first, "first Filter");
        Check.notNull(second, "second Filter");
        return new OrFilter(first, second);
    }

    @NotNull
    public static Filter and(@NotNull Filter first, @NotNull Filter second) {
        Check.notNull(first, "first Filter");
        Check.notNull(second, "second Filter");
        return new AndFilter(first, second);
    }

    @NotNull
    public static Filter field(@NotNull String fieldName, @NotNull Object value) {
        Check.notNull(fieldName, "fieldName");
        Check.notNull(value, "value");
        return new FieldFilter(fieldName, value);
    }
}
