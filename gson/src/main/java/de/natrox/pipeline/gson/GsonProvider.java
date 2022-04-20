package de.natrox.pipeline.gson;

import de.natrox.pipeline.json.JsonProvider;
import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.json.serializer.PipelineDataSerializer;

public final class GsonProvider implements JsonProvider {

    private final JsonDocument.Factory documentFactory = new GsonDocumentFactory();
    private final PipelineDataSerializer.Factory serializerFactory = new GsonPipelineDataSerializerFactory();

    @Override
    public JsonDocument.Factory documentFactory() {
        return documentFactory;
    }

    @Override
    public PipelineDataSerializer.Factory serializerFactory() {
        return serializerFactory;
    }
}
