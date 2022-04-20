package de.natrox.pipeline.test;

import de.natrox.pipeline.json.JsonFileConfig;
import de.natrox.pipeline.json.JsonFileProvider;

import java.nio.file.Path;

public class JsonFileTest {

    public static void main(String[] args) throws Exception {
JsonFileConfig jsonFileConfig = JsonFileConfig
    .builder()
    .path(Path.of("storage"))
    .build();
JsonFileProvider jsonFileProvider = jsonFileConfig.createProvider();
    }

}
