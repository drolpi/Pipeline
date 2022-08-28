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

import de.natrox.pipeline.exception.ConditionException;
import org.jetbrains.annotations.NotNull;

abstract class ComparableCondition extends DataNodeCondition {

    protected ComparableCondition(Object value, Object[] path) {
        super(value, path);
    }

    @SuppressWarnings("rawtypes")
    public @NotNull Comparable comparable() {
        Object value = super.value();

        if (!(value instanceof Comparable comparable))
            throw new ConditionException("value parameter must not be null");

        return comparable;
    }
}
