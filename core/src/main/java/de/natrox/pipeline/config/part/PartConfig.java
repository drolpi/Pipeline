package de.natrox.pipeline.config.part;

import de.natrox.pipeline.part.PartProvider;
import org.jetbrains.annotations.NotNull;

public interface PartConfig<T extends PartProvider> {

    @NotNull T createProvider() throws Exception;

}
