package de.natrox.pipeline.json.serializer;

import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.json.document.JsonDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public interface PipelineDataSerializer {

    @UnknownNullability PipelineData toPipelineData(@NotNull JsonDocument document);

    interface Factory {

        PipelineDataSerializer create(PipelineData data);

    }

}
