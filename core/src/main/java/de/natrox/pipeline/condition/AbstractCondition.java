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

import de.natrox.common.validate.Check;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCondition implements Condition {

    @Override
    public Condition and(@NotNull Condition condition) {
        Check.notNull(condition, "condition");
        return new AndCondition(this, condition);
    }

    @Override
    public Condition or(@NotNull Condition condition) {
        Check.notNull(condition, "condition");
        return new OrCondition(this, condition);
    }

    @Override
    public Condition not() {
        return new NotCondition(this);
    }
}
