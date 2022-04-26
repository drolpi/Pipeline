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

public abstract class FieldCondition extends AbstractCondition {

    private final String field;
    private final Object value;
    private boolean processed = false;

    protected FieldCondition(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    public String field() {
        return this.field;
    }

    public Object value() {
        if (this.processed)
            return value;

        if (value == null)
            return null;

        this.processed = true;
        return value;
    }

    public boolean processed() {
        return this.processed;
    }
}
