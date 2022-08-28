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

package de.natrox.pipeline.part.updater.event;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.node.DataNode;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class NodeUpdateEvent extends EntryEvent {

    private final DataNode node;

    public NodeUpdateEvent(@NotNull UUID senderId, @NotNull String repositoryName, @NotNull UUID documentId, @NotNull DataNode node) {
        super(senderId, repositoryName, documentId);
        Check.notNull(node, "node");
        this.node = node;
    }

    public @NotNull DataNode node() {
        return this.node;
    }
}

