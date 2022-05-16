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

package de.natrox.pipeline.part;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Store {

    @NotNull StoreMap openMap(@NotNull String mapName);

    @NotNull Set<String> maps();

    boolean hasMap(@NotNull String mapName);

    void closeMap(@NotNull String mapName);

    void removeMap(@NotNull String mapName);

    boolean isClosed();

    void close();
}
