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

package de.natrox.pipeline.redis;

import de.natrox.pipeline.part.updater.DocumentEvent;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.UUID;

final class RedisDocumentUpdateEvent extends DocumentEvent {

    private final String documentData;

    public RedisDocumentUpdateEvent(@NotNull UUID senderId, @NotNull String repositoryName, @NotNull UUID documentId, String documentData) {
        super(senderId, repositoryName, documentId);
        this.documentData = documentData;
    }

    public String documentData() {
        return this.documentData;
    }
}
