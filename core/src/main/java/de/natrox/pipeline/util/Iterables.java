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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class Iterables {

    private Iterables() {
        throw new UnsupportedOperationException();
    }

    public static <T> T firstOrNull(Iterable<T> iterable) {
        if (iterable == null) return null;

        Iterator<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    public static <T> List<T> toList(Iterable<T> iterable) {
        if (iterable instanceof List)
            return (List<T>) iterable;
        List<T> list = new ArrayList<>();
        for (T item : iterable) {
            list.add(item);
        }
        return list;
    }

    public static <T> Set<T> toSet(Iterable<T> iterable) {
        if (iterable instanceof Set)
            return (Set<T>) iterable;
        Set<T> set = new LinkedHashSet<>();
        for (T item : iterable) {
            set.add(item);
        }
        return set;
    }
}
