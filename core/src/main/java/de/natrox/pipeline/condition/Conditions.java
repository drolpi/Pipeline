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

package de.natrox.pipeline.condition;

import org.jetbrains.annotations.NotNull;

public final class Conditions {

    public static Condition ALL = element -> true;

    private Conditions() {
        throw new UnsupportedOperationException();
    }

    public static Condition eq(@NotNull String field, @NotNull Object value) {
        return new EqualsCondition(field, value);
    }

    public static Condition and(Condition @NotNull... conditions) {
        return new AndCondition(conditions);
    }

    public static Condition or(Condition @NotNull... conditions) {
        return new OrCondition(conditions);
    }

}
