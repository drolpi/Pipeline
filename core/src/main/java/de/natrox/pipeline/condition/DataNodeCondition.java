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

import de.natrox.pipeline.node.DataNode;

abstract class DataNodeCondition implements Condition {

    private final Object value;
    private final Object[] path;
    private boolean processed = false;

    protected DataNodeCondition(Object value, Object[] path) {
        this.value = value;
        this.path = path;
    }

    public Object[] path() {
        return this.path;
    }

    public Object value() {
        if (this.processed)
            return value;

        if (this.value == null)
            return null;

        this.processed = true;
        return this.value;
    }

    public DataNode findNode(DataNode rootNode) {
        return rootNode.node(this.path);
    }

    public boolean processed() {
        return this.processed;
    }
}
