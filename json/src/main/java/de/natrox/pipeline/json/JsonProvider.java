package de.natrox.pipeline.json;

import com.google.common.base.Preconditions;
import de.natrox.common.io.FileUtil;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public final class JsonProvider implements GlobalStorageProvider {

    private final JsonConfig config;
    private final Path path;

    protected JsonProvider(@NotNull JsonConfig config) {
        Preconditions.checkNotNull(config, "config");
        this.config = config;
        this.path = Path.of(config.path());
    }

    @Override
    public boolean init() {
        var parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            FileUtil.createDirectory(parent);
        }

        return true;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new JsonStorage(pipeline, path);
    }

}
