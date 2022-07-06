package de.natrox.pipeline.part.memory;

import de.natrox.pipeline.part.provider.LocalStorageProvider;
import org.jetbrains.annotations.NotNull;

public sealed interface InMemoryProvider extends LocalStorageProvider permits InMemoryProviderImpl {

    static @NotNull InMemoryProvider create() {
        return new InMemoryProviderImpl();
    }
}
