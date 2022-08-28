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
import de.natrox.pipeline.exception.ConditionException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public final class Conditions {

    private Conditions() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull FluentCondition where(Object @NotNull ... path) {
        Check.notNull(path, "path");
        Check.argCondition(path.length <= 0, "path");
        return new FluentCondition(path);
    }

    public static @NotNull Condition and(Condition @NotNull ... conditions) {
        Check.notNull(conditions, "conditions");
        if (conditions.length < 2) {
            throw new ConditionException("At least two conditions must be specified");
        }
        return new AndCondition(conditions);
    }

    public static @NotNull Condition or(Condition @NotNull ... conditions) {
        Check.notNull(conditions, "conditions");
        if (conditions.length < 2) {
            throw new ConditionException("At least two conditions must be specified");
        }
        return new OrCondition(conditions);
    }

    public static @NotNull Condition not(@NotNull Condition condition) {
        Check.notNull(condition, "condition");
        return new NotCondition(condition);
    }
}
