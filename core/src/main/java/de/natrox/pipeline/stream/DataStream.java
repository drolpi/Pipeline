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

package de.natrox.pipeline.stream;

import de.natrox.common.container.Pair;
import de.natrox.common.validate.Check;
import de.natrox.pipeline.util.Iterables;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@ApiStatus.Experimental
public interface DataStream<T> extends Iterable<T> {

    static <T> DataStream<T> fromIterable(@NotNull Iterable<T> iterable) {
        Check.notNull(iterable, "iterable");
        return iterable::iterator;
    }

    static <T, U> DataStream<Pair<T, U>> fromMap(Map<T, U> map) {
        Check.notNull(map, "map");
        return DataStream.fromIterable(() -> new PairIterator<>(map));
    }

    static <V> DataStream<V> single(@NotNull V v) {
        Check.notNull(v, "value");
        return DataStream.fromIterable(Collections.singleton(v));
    }

    static <V> DataStream<V> empty() {
        return DataStream.fromIterable(Collections.emptySet());
    }

    default @NotNull List<T> toList() {
        return Collections.unmodifiableList(Iterables.toList(this));
    }

    default @NotNull Set<T> toSet() {
        return Collections.unmodifiableSet(Iterables.toSet(this));
    }

    default @NotNull Optional<T> first() {
        return Optional.ofNullable(Iterables.firstOrNull(this));
    }

    default long size() {
        return Iterables.size(this);
    }

    default boolean isEmpty() {
        return !iterator().hasNext();
    }
}
