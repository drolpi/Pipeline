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

package de.natrox.pipeline.document;

import de.natrox.common.container.Pair;
import de.natrox.common.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@ApiStatus.Experimental
public sealed interface Document extends Iterable<Pair<String, Object>> permits DocumentImpl {

    static @NotNull Document create() {
        return new DocumentImpl();
    }

    static @NotNull Document create(@NotNull String key, @NotNull Object value) {
        Check.notNull(key, "key");
        Check.notNull(value, "value");

        LinkedHashMap<String, Object> document = new LinkedHashMap<>();
        document.put(key, value);
        return Document.create(document);
    }

    static @NotNull Document create(Map<String, Object> documentMap) {
        Check.notNull(documentMap, "documentMap");
        LinkedHashMap<String, Object> document = new LinkedHashMap<>(documentMap);
        return new DocumentImpl(document);
    }

    @NotNull Document put(@NotNull String key, @NotNull Object value);

    @Nullable Object get(@NotNull String key);

    <T> @Nullable T get(@NotNull String key, @NotNull Class<T> type);

    @NotNull UUID uniqueId();

    void remove(@NotNull String key);

    int size();

    boolean containsKey(@NotNull String key);

}
