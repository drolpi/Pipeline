package de.notion.pipeline.part.local.updater.def;

import de.notion.pipeline.datatype.PipelineData;
import de.notion.pipeline.part.local.updater.DataUpdater;
import de.notion.pipeline.part.local.updater.DataUpdaterService;
import org.jetbrains.annotations.NotNull;

public class DefaultDataUpdaterService implements DataUpdaterService {

    //TODO: Cache data updaters for specific data classes

    @Override
    public DataUpdater dataUpdater(@NotNull Class<? extends PipelineData> dataClass) {
        return new DefaultDataUpdater();
    }
}
