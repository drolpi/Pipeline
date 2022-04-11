package de.natrox.pipeline.config.connection;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.storage.GlobalStorage;

public interface GlobalStorageConnection extends Connection {

    GlobalStorage constructGlobalStorage(Pipeline pipeline);

}
