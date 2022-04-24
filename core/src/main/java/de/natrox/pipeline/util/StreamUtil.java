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

package de.natrox.pipeline.util;

import de.natrox.common.container.Pair;
import de.natrox.pipeline.stream.PipelineStream;

import java.util.Iterator;
import java.util.Map;

public final class StreamUtil {

    private StreamUtil() {
        throw new UnsupportedOperationException();
    }

    public static <T, U> PipelineStream<Pair<T, U>> streamForMap(Map<T, U> primaryMap) {
        return PipelineStream.fromIterable(() -> new Iterator<Pair<T, U>>() {
            private final Iterator<Map.Entry<T, U>> entryIterator =
                primaryMap.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return entryIterator.hasNext();
            }

            @Override
            public Pair<T, U> next() {
                Map.Entry<T, U> entry = entryIterator.next();
                return new Pair<>(entry.getKey(), entry.getValue());
            }
        });
    }
}
