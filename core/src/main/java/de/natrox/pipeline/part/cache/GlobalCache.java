package de.natrox.pipeline.part.cache;

import de.natrox.pipeline.datatype.PipelineData;
import de.natrox.pipeline.part.DataProvider;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface GlobalCache extends DataProvider {

    void updateExpireTime(@NotNull Class<? extends PipelineData> dataClass, @NotNull UUID objectUUID);

}
