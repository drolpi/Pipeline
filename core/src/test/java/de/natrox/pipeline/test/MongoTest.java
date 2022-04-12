package de.natrox.pipeline.test;

import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.mongodb.MongoConfig;

public class MongoTest {

    public static void main(String[] args) throws Exception {
        var mongoConfig = MongoConfig
            .builder()
            .host("127.0.0.1")
            .port(27017)
            .database("test")
            .build();
        var mongoProvider = mongoConfig.createProvider();

        var registry = new PipelineRegistry();
        var pipeline = Pipeline
            .builder()
            .registry(registry)
            .globalStorage(mongoProvider)
            .build();
    }

}
