package de.notion.pipeline.example.test;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.config.PipelineConfig;
import de.notion.pipeline.connection.ConnectionManager;
import de.notion.pipeline.registry.PipelineRegistry;
import de.notion.pipeline.json.JsonConfig;
import de.notion.pipeline.mongodb.MongoConfig;
import de.notion.pipeline.redis.RedisConfig;
import de.notion.pipeline.sql.h2.H2Config;
import de.notion.pipeline.sql.mysql.MySqlConfig;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

public class DefaultTest {

    public static void main(String[] args) {
        RedisConfig redisConfig = new RedisConfig(false, "", "redis://127.0.0.1:6379");
        MongoConfig mongoConfig = new MongoConfig("127.0.0.1", 27017, "test_data");
        JsonConfig jsonConfig = new JsonConfig(Paths.get("database"));
        H2Config h2Config = new H2Config(Paths.get("database"));
        MySqlConfig mySqlConfig = new MySqlConfig("127.0.0.1", 3306, true, "test_data", "root", "");

        PipelineConfig config = PipelineConfig
                .builder()
                .updater(redisConfig)
                .globalCache(redisConfig)
                .globalStorage(mySqlConfig)
                .build();

        PipelineRegistry registry = new PipelineRegistry();
        registry.register(TestData.class);
        registry.register(TestConnectionData.class);

        Pipeline pipeline = Pipeline.create(config, registry);
        pipeline.preloadAllData();

        pipeline.saveAllData();
        pipeline.shutdown();
    }

}
