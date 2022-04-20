package de.natrox.pipeline.gson;

import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.json.serializer.PipelineDataSerializer;

final class GsonPipelineDataSerializerFactory implements PipelineDataSerializer.Factory {

    protected GsonPipelineDataSerializerFactory() {

    }

    @Override
    public PipelineDataSerializer create(PipelineData data) {
        return new GsonPipelineDataSerializer(data);
    }
}
