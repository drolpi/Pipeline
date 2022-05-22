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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class NotConditionTest {

    @Test
    public void testApply() {
        Condition condition = mock(Condition.class);
        when(condition.apply(any())).thenReturn(true);

        NotCondition notCondition = new NotCondition(condition);
        assertFalse(notCondition.apply(Pair.empty()));

        verify(condition).apply(any());
    }

    @Test
    public void testApply2() {
        Condition condition = mock(Condition.class);
        when(condition.apply(any())).thenReturn(false);

        NotCondition notCondition = new NotCondition(condition);
        assertTrue(notCondition.apply(Pair.empty()));

        verify(condition).apply(any());
    }

}
