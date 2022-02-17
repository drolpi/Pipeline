package de.notion.pipeline.json;

import de.notion.pipeline.config.PartConfig;
import de.notion.pipeline.config.part.GlobalStorageConfig;
import de.notion.pipeline.json.storage.JsonStorage;
import de.notion.pipeline.part.storage.GlobalStorage;

import java.io.File;
import java.nio.file.Path;

public class JsonConfig implements GlobalStorageConfig, PartConfig {

    private final String jsonDbFile;

    private boolean connected;

    public JsonConfig(Path jsonDbFile) {
        this.jsonDbFile = jsonDbFile.toString();
    }

    @Deprecated
    @Override
    public GlobalStorage constructGlobalStorage() {
        return new JsonStorage(jsonDbFile);
    }

    @Override
    public void load() {
        File file = new File(jsonDbFile);
        if(!file.exists()) {
            file.mkdirs();
        }

        connected = true;
    }

    @Override
    public boolean isLoaded() {
        return connected;
    }

    @Override
    public void shutdown() {
        connected = false;
    }
}
