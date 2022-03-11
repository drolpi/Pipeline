package de.notion.pipeline.config.connection;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.part.cache.GlobalCache;

public interface GlobalCacheConnection extends Connection {

    GlobalCache constructGlobalCache(Pipeline pipeline);

}
