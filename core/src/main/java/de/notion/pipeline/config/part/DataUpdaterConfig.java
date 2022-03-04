package de.notion.pipeline.config.part;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.config.PartConfig;
import de.notion.pipeline.part.local.updater.DataUpdaterService;

public interface DataUpdaterConfig extends PartConfig {

    DataUpdaterService constructDataUpdaterService(Pipeline pipeline);

}
