package de.notion.pipeline.config.connection;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.part.local.updater.DataUpdaterService;

public interface DataUpdaterConnection extends Connection {

    DataUpdaterService constructDataUpdaterService(Pipeline pipeline);

}