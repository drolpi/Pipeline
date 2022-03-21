package de.natrox.pipeline.config.connection;

import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.Pipeline;

public interface GlobalStorageConnection extends Connection {

    GlobalStorage constructGlobalStorage(Pipeline pipeline);

}
