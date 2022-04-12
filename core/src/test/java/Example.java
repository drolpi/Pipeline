import de.natrox.common.logger.LogManager;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.config.PipelineConfig;
import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.h2.H2Config;
import de.natrox.pipeline.json.JsonConfig;
import de.natrox.pipeline.mongodb.MongoConfig;
import de.natrox.pipeline.mysql.MySqlConfig;
import de.natrox.pipeline.mysql.MySqlEndpoint;
import de.natrox.pipeline.redis.RedisConfig;
import de.natrox.pipeline.redis.RedisEndpoint;
import de.natrox.pipeline.sqllite.SQLiteConfig;

import java.nio.file.Path;
import java.util.UUID;

public class Example {

    public static void main(String[] args) throws Exception {
        LogManager.setDebug(true);

        var configLoader = new Object();
        var config = new PipelineConfig();

        var dataUpdaterProvider = config.dataUpdater() != null ? config.dataUpdater().createProvider() : null;

        var redisConfig = RedisConfig
            .builder()
            .username("")
            .password("")
            .endpoints(
                RedisEndpoint
                    .builder()
                    .host("127.0.0.1")
                    .port(6379)
                    .database(0)
                    .build()
            )
            .build();
        var redisProvider = redisConfig.createProvider();

        var globalCacheProvider = config.globalCache() != null ? config.globalCache().createProvider() : null;

        var h2Config = H2Config
            .builder()
            .path(Path.of("D:", "Dev", "database"))
            .build();
        var h2Provider = h2Config.createProvider();

        var jsonConfig = JsonConfig
            .builder()
            .path(Path.of("D:", "Dev", "database"))
            .build();
        var jsonProvider = jsonConfig.createProvider();

        var mongoConfig = MongoConfig
            .builder()
            .host("127.0.0.1")
            .port(27017)
            .database("test")
            .build();
        var mongoProvider = mongoConfig.createProvider();

        var mySqlConfig = MySqlConfig
            .builder()
            .endpoints(
                MySqlEndpoint
                    .builder()
                    .host("127.0.0.1")
                    .port(3306)
                    .database("test")
                    .build()
            )
            .username("root")
            .password("")
            .build();
        var mySqlProvider = mySqlConfig.createProvider();

        var sQLiteConfig = SQLiteConfig
            .builder()
            .path(Path.of("D:", "Dev", "db.sqlite"))
            .build();
        var sQLiteProvider = sQLiteConfig.createProvider();

        var globalStorageProvider = config.globalStorage() != null ? config.globalStorage().createProvider() : null;

        var registry = new PipelineRegistry();
        registry.register(Player.class);

        var pipeline = Pipeline
            .builder()
            .registry(registry)
            .globalCache(redisProvider)
            .build();

        var uuid = UUID.randomUUID();
        pipeline.load(Player.class, uuid, Pipeline.LoadingStrategy.LOAD_PIPELINE, (dataClass, pipeline1) -> new Player(pipeline1, "Niklas", 5),true);
    }

}
