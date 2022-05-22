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

public class AndConditionTest {

    @Test
    public void testApply() {
        Condition condition1 = mock(Condition.class);
        when(condition1.apply(any())).thenReturn(true);

        Condition condition2 = mock(Condition.class);
        when(condition2.apply(any())).thenReturn(true);

        Condition condition3 = mock(Condition.class);
        when(condition3.apply(any())).thenReturn(true);

        AndCondition andCondition = new AndCondition(condition1, condition2, condition3);
        assertTrue(andCondition.apply(Pair.empty()));

        verify(condition1).apply(any());
        verify(condition2).apply(any());
        verify(condition3).apply(any());
    }

    @Test
    public void testApply2() {
        Condition condition1 = mock(Condition.class);
        when(condition1.apply(any())).thenReturn(false);

        Condition condition2 = mock(Condition.class);
        when(condition2.apply(any())).thenReturn(true);

        Condition condition3 = mock(Condition.class);
        when(condition3.apply(any())).thenReturn(true);

        AndCondition andCondition = new AndCondition(condition1, condition2, condition3);
        assertFalse(andCondition.apply(Pair.empty()));

        verify(condition1).apply(any());
    }

}
