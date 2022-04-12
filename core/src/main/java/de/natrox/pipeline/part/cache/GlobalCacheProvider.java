package de.natrox.pipeline.part.cache;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.PartProvider;

public interface GlobalCacheProvider extends PartProvider {

    GlobalCache constructGlobalCache(Pipeline pipeline);

}
