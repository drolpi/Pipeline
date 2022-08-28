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
import de.natrox.pipeline.exception.ConditionException;
import de.natrox.pipeline.node.DataNode;
import de.natrox.pipeline.util.Numbers;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

final class LesserEqualCondition extends ComparableCondition {

    LesserEqualCondition(Comparable<?> value, Object[] path) {
        super(value, path);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean apply(@NotNull Pair<UUID, DataNode> element) {
        Comparable comparable = super.comparable();
        DataNode node = element.second();
        DataNode subNode = super.findNode(node);

        Object nodeValue = subNode.getAs();
        if (nodeValue == null) {
            return false;
        }

        if (nodeValue instanceof Number numberValue && comparable instanceof Number numberComparable) {
            return Numbers.compare(numberValue, numberComparable) <= 0;
        } else if (nodeValue instanceof Comparable numberValue) {
            return numberValue.compareTo(comparable) <= 0;
        }

        throw new ConditionException(nodeValue + " is not comparable");
    }
}
