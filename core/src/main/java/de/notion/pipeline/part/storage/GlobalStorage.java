package de.notion.pipeline.part.storage;

import com.google.gson.JsonObject;
import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.operator.FindOptions;
import de.notion.pipeline.operator.filter.Filter;
import de.notion.pipeline.part.DataProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GlobalStorage extends DataProvider {

    @NotNull
    Map<UUID, JsonObject> data(@NotNull Class<? extends PipelineData> type);

}
