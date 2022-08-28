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

    private final Object[] path;

    FluentCondition(Object[] path) {
        this.path = path;
    }

    public @NotNull Condition eq(@NotNull Object value) {
        Check.notNull(value, "value");
        return new EqualsCondition(value, path);
    }

    public @NotNull Condition notEq(@NotNull Object value) {
        Check.notNull(value, "value");
        return new NotEqualsCondition(value, path);
    }

    public @NotNull Condition gt(@NotNull Comparable<?> value) {
        Check.notNull(value, "value");
        return new GreaterThanCondition(value, path);
    }

    public @NotNull Condition gte(@NotNull Comparable<?> value) {
        Check.notNull(value, "value");
        return new GreaterEqualCondition(value, path);
    }

    public @NotNull Condition lt(@NotNull Comparable<?> value) {
        Check.notNull(value, "value");
        return new LesserThanCondition(value, path);
    }

    public @NotNull Condition lte(@NotNull Comparable<?> value) {
        Check.notNull(value, "value");
        return new LesserEqualCondition(value, path);
    }
}
