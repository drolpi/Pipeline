package de.natrox.pipeline.sqllite;

import com.google.common.base.Preconditions;
import de.natrox.pipeline.config.part.ConfigBuilder;
import de.natrox.pipeline.config.part.PartConfig;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public final class SQLiteConfig implements PartConfig<SQLiteProvider> {

    private final String path;

    private SQLiteConfig(@NotNull Path path) {
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
    public @NotNull SQLiteProvider createProvider() {
        return new SQLiteProvider(this);
    }

    public static class Builder implements ConfigBuilder<SQLiteConfig> {

        private Path path;

        private Builder() {

        }

        public @NotNull Builder path(@NotNull Path path) {
            Preconditions.checkNotNull(path, "path");
            this.path = path;
            return this;
        }

        @Override
        public @NotNull SQLiteConfig build() {
            return new SQLiteConfig(path);
        }
    }

}
