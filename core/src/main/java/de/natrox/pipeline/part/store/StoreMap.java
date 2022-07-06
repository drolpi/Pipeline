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

package de.natrox.pipeline.part.store;

import de.natrox.common.validate.Check;
import de.natrox.pipeline.repository.QueryStrategy;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ApiStatus.Internal
public interface StoreMap {

    byte @Nullable [] get(@NotNull UUID uniqueId);

    void put(@NotNull UUID uniqueId, byte @NotNull [] data);

    default boolean contains(@NotNull UUID uniqueId, QueryStrategy @NotNull ... strategies) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(strategies, "strategies");
        Check.argCondition(strategies.length <= 0, "strategies");
        return this.contains(uniqueId, Set.of(strategies));
    }

    boolean contains(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies);

    @NotNull Collection<UUID> keys();

    @NotNull Collection<byte[]> values();

    @NotNull Map<UUID, byte[]> entries();

    default void remove(@NotNull UUID uniqueId, QueryStrategy @NotNull ... strategies) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(strategies, "strategies");
        Check.argCondition(strategies.length <= 0, "strategies");
        this.remove(uniqueId, Set.of(strategies));
    }

    void remove(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies);

    void clear();

    long size();

}
