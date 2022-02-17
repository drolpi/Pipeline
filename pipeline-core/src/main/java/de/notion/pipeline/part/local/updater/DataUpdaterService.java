package de.notion.pipeline.part.local.updater;

import de.notion.pipeline.datatype.PipelineData;
import org.jetbrains.annotations.NotNull;

public interface DataUpdaterService {

    DataUpdater dataUpdater(@NotNull Class<? extends PipelineData> dataClass);

}
