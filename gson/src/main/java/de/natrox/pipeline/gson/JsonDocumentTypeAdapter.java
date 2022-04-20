package de.natrox.pipeline.gson;

import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@Internal
final class JsonDocumentTypeAdapter extends TypeAdapter<GsonDocument> {

    @Override
    public void write(JsonWriter jsonWriter, GsonDocument document) throws IOException {
        TypeAdapters.JSON_ELEMENT.write(jsonWriter, document == null ? new JsonObject() : document.object);
    }

    @Override
    public @Nullable GsonDocument read(JsonReader jsonReader) throws IOException {
        var jsonElement = TypeAdapters.JSON_ELEMENT.read(jsonReader);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            return new GsonDocument(jsonElement.getAsJsonObject());
        } else {
            return null;
        }
    }
}
