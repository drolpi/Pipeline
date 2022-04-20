package de.natrox.pipeline.json;

import com.google.common.base.Preconditions;
import de.natrox.common.builder.IBuilder;
import de.natrox.pipeline.config.part.PartConfig;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class JsonFileConfig implements PartConfig<JsonFileProvider> {

    private final String path;

    private JsonFileConfig(@NotNull Path path) {
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
    public @NotNull JsonFileProvider createProvider() throws Exception {
        return new JsonFileProvider(this);
    }

    public static class Builder implements IBuilder<JsonFileConfig> {

        private Path path;

        private Builder() {

        }

        public @NotNull Builder path(@NotNull Path path) {
            Preconditions.checkNotNull(path, "path");
            this.path = path;
            return this;
        }

        @Override
        public @NotNull JsonFileConfig build() {
            return new JsonFileConfig(path);
        }
    }

}
