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

package de.natrox.pipeline.condition;

import de.natrox.common.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
@SuppressWarnings("ClassCanBeRecord")
public final class FluentCondition {

    private final String field;

    private FluentCondition(String field) {
        this.field = field;
    }

    public static FluentCondition where(@NotNull String field) {
        Check.notNull(field, "field");
        return new FluentCondition(field);
    }

    public Condition eq(@NotNull Object value) {
        Check.notNull(value, "value");
        return new EqualsCondition(field, value);
    }

    public Condition notEq(@NotNull Object value) {
        Check.notNull(value, "value");
        return new NotEqualsCondition(field, value);
    }

    public Condition gt(@NotNull Comparable<?> value) {
        Check.notNull(value, "value");
        return new GreaterThanCondition(field, value);
    }

    public Condition gte(@NotNull Comparable<?> value) {
        Check.notNull(value, "value");
        return new GreaterEqualCondition(field, value);
    }

    public Condition lt(@NotNull Comparable<?> value) {
        Check.notNull(value, "value");
        return new LesserThanCondition(field, value);
    }

    public Condition lte(@NotNull Comparable<?> value) {
        Check.notNull(value, "value");
        return new LesserEqualCondition(field, value);
    }

}
