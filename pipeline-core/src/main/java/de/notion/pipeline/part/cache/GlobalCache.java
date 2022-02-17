package de.notion.pipeline.part.cache;

import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.DataProvider;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface GlobalCache extends DataProvider {

    void updateExpireTime(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID);

}
