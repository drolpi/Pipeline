package de.natrox.pipeline.part.storage;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.PartProvider;

public interface GlobalStorageProvider extends PartProvider {

    GlobalStorage constructGlobalStorage(Pipeline pipeline);

}
