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

import java.util.*;

@ApiStatus.Internal
public interface StoreMap {

    @Nullable Object get(@NotNull UUID uniqueId);

    void put(@NotNull UUID uniqueId, @NotNull Object data, @NotNull Set<QueryStrategy> strategies);

    default void put(@NotNull UUID uniqueId, @NotNull Object data, QueryStrategy @NotNull ... strategies) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(data, "data");
        Check.notNull(strategies, "strategies");
        Set<QueryStrategy> strategySet = new HashSet<>(Set.of(strategies));
        if(strategySet.size() <= 0) {
            strategySet.add(QueryStrategy.ALL);
        }

        this.put(uniqueId, data, strategySet);
    }

    boolean contains(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies);

    default boolean contains(@NotNull UUID uniqueId, QueryStrategy @NotNull ... strategies) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(strategies, "strategies");
        Set<QueryStrategy> strategySet = new HashSet<>(Set.of(strategies));
        if(strategySet.size() <= 0) {
            strategySet.add(QueryStrategy.ALL);
        }

        return this.contains(uniqueId, strategySet);
    }

    @NotNull Collection<UUID> keys();

    @NotNull Collection<Object> values();

    @NotNull Map<UUID, Object> entries();

    void remove(@NotNull UUID uniqueId, @NotNull Set<QueryStrategy> strategies);

    default void remove(@NotNull UUID uniqueId, QueryStrategy @NotNull ... strategies) {
        Check.notNull(uniqueId, "uniqueId");
        Check.notNull(strategies, "strategies");
        Set<QueryStrategy> strategySet = new HashSet<>(Set.of(strategies));
        if(strategySet.size() <= 0) {
            strategySet.add(QueryStrategy.ALL);
        }
        this.remove(uniqueId, strategySet);
    }

    void clear();

    long size();

}
