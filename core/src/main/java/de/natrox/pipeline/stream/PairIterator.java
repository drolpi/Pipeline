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

import java.util.Iterator;
import java.util.Map;

final class PairIterator<T, U> implements Iterator<Pair<T, U>> {

    private final Iterator<Map.Entry<T, U>> entryIterator;

    public PairIterator(Map<T, U> map) {
        this.entryIterator = map.entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
        return this.entryIterator.hasNext();
    }

    @Override
    public Pair<T, U> next() {
        Map.Entry<T, U> entry = this.entryIterator.next();
        return new Pair<>(entry.getKey(), entry.getValue());
    }
}
