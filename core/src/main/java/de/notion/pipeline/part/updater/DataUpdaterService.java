package de.notion.pipeline.part.updater;

import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

public interface DataUpdaterService {

    @NotNull
    DataUpdater dataUpdater(@NotNull Class<? extends PipelineData> dataClass);

}
