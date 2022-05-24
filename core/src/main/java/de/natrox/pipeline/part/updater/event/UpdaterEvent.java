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
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.UUID;

public abstract class UpdaterEvent implements Serializable {

    private final UUID senderId;
    private final String repositoryName;

    public UpdaterEvent(@NotNull UUID senderId, @NotNull String repositoryName) {
        Check.notNull(senderId, "senderId");
        Check.notNull(repositoryName, "repositoryName");
        this.senderId = senderId;
        this.repositoryName = repositoryName;
    }

    public @NotNull UUID senderId() {
        return this.senderId;
    }

    public @NotNull String repositoryName() {
        return this.repositoryName;
    }
}
