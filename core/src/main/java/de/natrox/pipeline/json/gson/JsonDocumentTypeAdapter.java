package de.natrox.pipeline.json.gson;

import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@Internal
final class JsonDocumentTypeAdapter extends TypeAdapter<JsonDocument> {

    @Override
    public void write(JsonWriter jsonWriter, JsonDocument document) throws IOException {
        TypeAdapters.JSON_ELEMENT.write(jsonWriter, document == null ? new JsonObject() : document.object);
    }

    @Override
    public @Nullable JsonDocument read(JsonReader jsonReader) throws IOException {
        var jsonElement = TypeAdapters.JSON_ELEMENT.read(jsonReader);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            return new JsonDocument(jsonElement.getAsJsonObject());
        } else {
            return null;
        }
    }
}
