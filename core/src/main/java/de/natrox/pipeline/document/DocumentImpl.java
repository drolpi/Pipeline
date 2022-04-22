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
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;

public final class DocumentImpl implements Document {

    public DocumentImpl() {

    }

    public DocumentImpl(LinkedHashMap<String, Object> document) {

    }

    @Override
    public @NotNull Document put(@NotNull String key, @NotNull Object value) {
        return null;
    }

    @Override
    public Object get(@NotNull String key) {
        return null;
    }

    @Override
    public <T> T get(@NotNull String key, @NotNull Class<T> type) {
        return null;
    }

    @Override
    public @NotNull UUID uniqueId() {
        return null;
    }

    @Override
    public void remove(@NotNull String key) {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean containsKey(@NotNull String key) {
        return false;
    }

    @NotNull
    @Override
    public Iterator<Pair<String, Object>> iterator() {
        return null;
    }
}
