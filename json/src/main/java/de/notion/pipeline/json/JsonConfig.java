package de.notion.pipeline.json;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.config.PartConfig;
import de.notion.pipeline.config.part.GlobalStorageConfig;
import de.notion.pipeline.json.storage.JsonStorage;
import de.notion.pipeline.part.storage.GlobalStorage;

import java.nio.file.Path;

public class JsonConfig implements GlobalStorageConfig, PartConfig {

    private final String jsonDbFile;
    private boolean connected;

    public JsonConfig(Path jsonDbFile) {
        this.jsonDbFile = jsonDbFile.toString();
    }

    @Override
    public GlobalStorage constructGlobalStorage(Pipeline pipeline) {
        return new JsonStorage(pipeline, jsonDbFile);
    }

    @Override
    public void load() {
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
