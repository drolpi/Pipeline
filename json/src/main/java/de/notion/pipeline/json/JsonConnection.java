package de.notion.pipeline.json;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.config.connection.Connection;
import de.notion.pipeline.config.connection.GlobalStorageConnection;
import de.notion.pipeline.json.storage.JsonStorage;
import de.notion.pipeline.part.storage.GlobalStorage;

import java.nio.file.Path;

public class JsonConnection implements GlobalStorageConnection, Connection {

    private final String jsonDbFile;
    private boolean connected;

    public JsonConnection(Path jsonDbFile) {
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
