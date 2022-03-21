package de.natrox.pipeline.json;

import de.natrox.pipeline.json.storage.JsonStorage;
import de.natrox.pipeline.part.storage.GlobalStorage;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.config.connection.Connection;
import de.natrox.pipeline.config.connection.GlobalStorageConnection;

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
