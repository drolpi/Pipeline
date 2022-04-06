package de.natrox.pipeline.config.connection;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.updater.DataUpdater;

public interface DataUpdaterConnection extends Connection {

    DataUpdater constructDataUpdater(Pipeline pipeline);

}
