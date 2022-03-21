package de.natrox.pipeline.config.connection;

import de.natrox.pipeline.part.cache.GlobalCache;
import de.natrox.pipeline.Pipeline;

public interface GlobalCacheConnection extends Connection {

    GlobalCache constructGlobalCache(Pipeline pipeline);

}
