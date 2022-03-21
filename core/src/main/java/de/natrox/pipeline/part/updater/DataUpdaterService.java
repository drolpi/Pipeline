package de.natrox.pipeline.part.updater;

import de.natrox.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

public interface DataUpdaterService {

    @NotNull
    DataUpdater dataUpdater(@NotNull Class<? extends PipelineData> dataClass);

}
