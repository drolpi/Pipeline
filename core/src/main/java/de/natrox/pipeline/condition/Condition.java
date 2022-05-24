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

import de.natrox.common.container.Pair;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.document.DocumentData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@ApiStatus.Experimental
@FunctionalInterface
public interface Condition {

    boolean apply(Pair<UUID, DocumentData> element);

    default @NotNull Condition and(@NotNull Condition condition) {
        Check.notNull(condition, "condition");
        return new AndCondition(this, condition);
    }

    default @NotNull Condition or(@NotNull Condition condition) {
        Check.notNull(condition, "condition");
        return new OrCondition(this, condition);
    }

    default @NotNull Condition not() {
        return new NotCondition(this);
    }

}
