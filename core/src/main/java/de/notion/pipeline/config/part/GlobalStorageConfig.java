package de.notion.pipeline.config.part;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.config.PartConfig;
import de.notion.pipeline.part.storage.GlobalStorage;

public interface GlobalStorageConfig extends PartConfig {

    GlobalStorage constructGlobalStorage(Pipeline pipeline);

}
