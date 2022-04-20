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

package de.natrox.pipeline.gson;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.natrox.pipeline.json.document.JsonDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class GsonDocumentFactory implements JsonDocument.Factory {

    private final static GsonDocument EMPTY = new GsonDocument();

    protected GsonDocumentFactory() {

    }

    @Override
    public @NotNull JsonDocument emptyDocument() {
        return EMPTY;
    }

    @Override
    public @NotNull JsonDocument newDocument() {
        return new GsonDocument();
    }

    @Override
    public @NotNull JsonDocument newDocument(@Nullable Object value) {
        return new GsonDocument(value == null ? new JsonObject() : GsonDocument.GSON.toJsonTree(value).getAsJsonObject());
    }

    @Override
    public @NotNull JsonDocument fromJsonString(@NotNull String json) {
        return new GsonDocument(JsonParser.parseString(json).getAsJsonObject());
    }
}
