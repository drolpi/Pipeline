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

import de.natrox.common.container.Pair;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.document.PipeDocument;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class Conditions {

    public static Condition ALL = new AbstractCondition() {
        @Override
        public boolean apply(Pair<UUID, PipeDocument> element) {
            return true;
        }
    };

    private Conditions() {
        throw new UnsupportedOperationException();
    }

    public static Condition eq(@NotNull String field, @NotNull Object value) {
        Check.notNull(field, "field");
        Check.notNull(value, "value");
        return new EqualsCondition(field, value);
    }

    public static Condition and(Condition @NotNull ... conditions) {
        Check.notNull(conditions, "conditions");
        Check.argCondition(conditions.length <= 0, "conditions");
        return new AndCondition(conditions);
    }

    public static Condition or(Condition @NotNull ... conditions) {
        Check.notNull(conditions, "conditions");
        Check.argCondition(conditions.length <= 0, "conditions");
        return new OrCondition(conditions);
    }

}
