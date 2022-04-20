package de.natrox.pipeline.json;

import com.google.common.base.Preconditions;
import de.natrox.common.io.FileUtil;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.part.storage.GlobalStorageProvider;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

public final class JsonFileProvider implements GlobalStorageProvider {

    private final Path path;

    protected JsonFileProvider(@NotNull JsonFileConfig config) throws Exception {
        Preconditions.checkNotNull(config, "config");
        this.path = Path.of(config.path());
        var parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            FileUtil.createDirectory(parent);
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new JsonFileStorage(pipeline, path);
    }

}
