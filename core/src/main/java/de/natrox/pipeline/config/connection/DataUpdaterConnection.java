package de.natrox.pipeline.config.connection;

import de.natrox.pipeline.part.updater.DataUpdaterService;
import de.natrox.pipeline.Pipeline;

public interface DataUpdaterConnection extends Connection {

    DataUpdaterService constructDataUpdaterService(Pipeline pipeline);

}
