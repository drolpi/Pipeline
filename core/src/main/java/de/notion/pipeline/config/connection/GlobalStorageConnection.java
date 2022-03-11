package de.notion.pipeline.config.connection;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.part.storage.GlobalStorage;

public interface GlobalStorageConnection extends Connection {

    GlobalStorage constructGlobalStorage(Pipeline pipeline);

}
