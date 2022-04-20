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
