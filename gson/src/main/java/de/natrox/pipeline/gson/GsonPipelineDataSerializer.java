package de.natrox.pipeline.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.json.serializer.PipelineDataSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

final class GsonPipelineDataSerializer implements PipelineDataSerializer {

    private final Gson gson;
    private final PipelineData data;

    protected GsonPipelineDataSerializer(PipelineData data) {
        this.data = data;
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(data.getClass(), (InstanceCreator<PipelineData>) type -> data)
            .create();
    }

    @Override
    public @UnknownNullability PipelineData toPipelineData(@NotNull JsonDocument document) {
        if (!(document instanceof GsonDocument gsonDocument))
            return null;
        return gson.fromJson(gsonDocument.object, data.getClass());
    }
}
