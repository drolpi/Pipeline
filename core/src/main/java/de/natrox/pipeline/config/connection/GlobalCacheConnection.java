package de.natrox.pipeline.config.connection;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.cache.GlobalCache;

public interface GlobalCacheConnection extends Connection {

    GlobalCache constructGlobalCache(Pipeline pipeline);

}
