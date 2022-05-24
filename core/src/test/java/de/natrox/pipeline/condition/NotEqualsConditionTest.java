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
import de.natrox.pipeline.document.DocumentData;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class NotEqualsConditionTest {

    @Test
    public void testConstructor() {
        NotEqualsCondition notEqualsCondition = new NotEqualsCondition("field", "value");

        assertEquals("field", notEqualsCondition.field());
    }

    @Test
    public void testApply() {
        NotEqualsCondition equalsCondition = new NotEqualsCondition("field", "value");
        Pair<UUID, DocumentData> pair = Pair.of(UUID.randomUUID(), DocumentData.create());

        assertTrue(equalsCondition.value() instanceof String);
        assertTrue(equalsCondition.apply(pair));
    }

    @Test
    public void testApply2() {
        NotEqualsCondition equalsCondition = new NotEqualsCondition("field", "value");

        assertTrue(equalsCondition.value() instanceof String);
        assertFalse(equalsCondition.apply(Pair.of(UUID.randomUUID(), DocumentData.create("field", "value"))));
    }

}
