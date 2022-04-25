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

package de.natrox.pipeline.document.action;

import de.natrox.pipeline.condition.Condition;
import de.natrox.pipeline.document.DocumentCursor;
import de.natrox.pipeline.document.PipeDocument;
import de.natrox.pipeline.part.map.PartMap;
import de.natrox.pipeline.stream.ConditionStream;
import de.natrox.pipeline.stream.DocumentStream;

import java.util.UUID;

public final class ReadActions {

    private final PartMap partMap;

    public ReadActions(PartMap partMap) {
        this.partMap = partMap;
    }

    public DocumentCursor find(Condition condition) {
        ConditionStream conditionStream = new ConditionStream(partMap.entries(), condition);
        return new DocumentStream(conditionStream);
    }

    public PipeDocument findById(UUID uniqueId) {
        return partMap.get(uniqueId);
    }

    public boolean contains(UUID uniqueId) {
        return partMap.contains(uniqueId);
    }

}
