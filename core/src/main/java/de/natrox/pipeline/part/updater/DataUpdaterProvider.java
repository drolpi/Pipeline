package de.natrox.pipeline.part.updater;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.PartProvider;

public interface DataUpdaterProvider extends PartProvider {

    DataUpdater constructDataUpdater(Pipeline pipeline);

}
