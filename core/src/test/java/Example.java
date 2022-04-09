import de.natrox.common.logger.LogManager;
import de.natrox.pipeline.Pipeline;
import de.natrox.pipeline.config.PipelineConfig;
import de.natrox.pipeline.config.PipelineRegistry;
import de.natrox.pipeline.h2.H2Connection;
import de.natrox.pipeline.mongodb.MongoConnection;
import de.natrox.pipeline.mysql.MySqlConnection;
import de.natrox.pipeline.redis.RedisConnection;
import de.natrox.pipeline.sqllite.SQLiteConnection;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

public class Example implements Serializable {

    private final static UUID TEST_ID = UUID.nameUUIDFromBytes("ID".getBytes(StandardCharsets.UTF_8));

    public static void main(String[] args) {
        LogManager.setDebug(true);

        var redisConnection = new RedisConnection(false, "", "redis://localhost:6379");
        var mongoConnection = new MongoConnection("localhost", 27017, "test");
        var h2Connection = new H2Connection(Path.of("db.h2"));
        var sqlLiteConnection = new SQLiteConnection(Path.of("db.sqlite"));
        var mysqlConnection = new MySqlConnection("localhost", 3306, false, "test", "root", "");

        var config = PipelineConfig
            .builder()
            .dataUpdater(redisConnection)
            .globalCache(redisConnection)
            .globalStorage(mongoConnection)
            .build();

        var registry = new PipelineRegistry();
        registry.register(Player.class);

        var pipeline = Pipeline.create(config, registry);

        var player = pipeline.load(Player.class, TEST_ID, Pipeline.LoadingStrategy.LOAD_PIPELINE, (dataClass, pipeline1) -> new Player(pipeline1, "Test", 5), true);
    }

}
