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
import de.natrox.pipeline.document.PipeDocument;
import de.natrox.pipeline.util.Numbers;

import java.util.UUID;

final class LesserEqualCondition extends ComparableCondition {

    LesserEqualCondition(String field, Comparable<?> value) {
        super(field, value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean apply(Pair<UUID, PipeDocument> element) {
        Comparable comparable = getComparable();
        PipeDocument document = element.second();
        Object fieldValue = document.get(field());
        if (fieldValue != null) {
            if (fieldValue instanceof Number && comparable instanceof Number) {
                return Numbers.compare((Number) fieldValue, (Number) comparable) <= 0;
            } else if (fieldValue instanceof Comparable arg) {
                return arg.compareTo(comparable) <= 0;
            } else {
                throw new RuntimeException(fieldValue + " is not comparable");
            }
        }

        return false;
    }
}
