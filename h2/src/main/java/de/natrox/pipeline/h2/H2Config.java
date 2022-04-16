package de.natrox.pipeline.h2;

import com.google.common.base.Preconditions;
import de.natrox.common.builder.IBuilder;
import de.natrox.pipeline.config.part.PartConfig;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class H2Config implements PartConfig<H2Provider> {

    private final String path;

    private H2Config(@NotNull Path path) {
        Preconditions.checkNotNull(path, "path");
        this.path = path.toAbsolutePath().toString();
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public String path() {
        return this.path;
    }

    @Override
    public @NotNull H2Provider createProvider() throws Exception {
        return new H2Provider(this);
    }

    public static class Builder implements IBuilder<H2Config> {

        private Path path;

        private Builder() {

        }

        public @NotNull Builder path(@NotNull Path path) {
            Preconditions.checkNotNull(path, "path");
            this.path = path;
            return this;
        }

        @Override
        public @NotNull H2Config build() {
            return new H2Config(path);
        }
    }

}
