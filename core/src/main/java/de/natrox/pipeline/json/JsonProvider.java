package de.natrox.pipeline.json;

import de.natrox.pipeline.json.document.JsonDocument;
import de.natrox.pipeline.json.serializer.PipelineDataSerializer;

public interface JsonProvider {

    JsonDocument.Factory documentFactory();

    PipelineDataSerializer.Factory serializerFactory();

}
