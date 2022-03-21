package de.natrox.pipeline.part.storage;

import com.google.gson.JsonObject;
import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.part.DataProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public interface GlobalStorage extends DataProvider {

    @NotNull
    Map<UUID, JsonObject> data(@NotNull Class<? extends PipelineData> type);

}
