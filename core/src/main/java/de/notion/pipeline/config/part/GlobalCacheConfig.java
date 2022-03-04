package de.notion.pipeline.config.part;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.config.PartConfig;
import de.notion.pipeline.part.cache.GlobalCache;

public interface GlobalCacheConfig extends PartConfig {

    GlobalCache constructGlobalCache(Pipeline pipeline);

}
