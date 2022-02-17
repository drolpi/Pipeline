package de.notion.pipeline.example.friend;

import de.notion.pipeline.Pipeline;
import de.notion.pipeline.config.PipelineConfig;
import de.notion.pipeline.filter.Filters;
import de.notion.pipeline.json.JsonConfig;
import de.notion.pipeline.mongodb.MongoConfig;
import de.notion.pipeline.redis.RedisConfig;
import de.notion.pipeline.registry.PipelineRegistry;
import de.notion.pipeline.sql.mysql.MySqlConfig;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class FriendManager {

    private final Pipeline pipeline;

    public FriendManager(Pipeline plugin) {
        this.pipeline = plugin;
    }

    public static void main(String[] args) throws InterruptedException {
        RedisConfig redisConfig = new RedisConfig(false, "", "redis://127.0.0.1:6379");
        MongoConfig mongoConfig = new MongoConfig("127.0.0.1", 27017, "test_data");
        MySqlConfig mySqlConfig = new MySqlConfig("127.0.0.1", 3306, false, "test_data", "root", "");
        JsonConfig jsonConfig = new JsonConfig(Paths.get("database"));

        PipelineConfig config = PipelineConfig
                .builder()
                .updater(redisConfig)
                .globalCache(redisConfig)
                .globalStorage(mongoConfig)
                .build();

        PipelineRegistry registry = new PipelineRegistry();
        registry.register(DefaultFriend.class);

        Pipeline pipeline = Pipeline.create(config, registry);

        FriendManager friendManager = new FriendManager(pipeline);
        long startTime3 = System.currentTimeMillis();

        UUID amubbm = UUID.nameUUIDFromBytes("AmuBBM".toUpperCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
        UUID drolpi = UUID.nameUUIDFromBytes("drolpi".toUpperCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));

        friendManager.list(drolpi)
                .thenAccept(friends -> System.out.println(friends.size()));

        System.out.println(System.currentTimeMillis() - startTime3 + "ms");
    }

    private static UUID getId(String name) {
        return UUID.nameUUIDFromBytes(name.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
    }

    public CompletableFuture<List<DefaultFriend>> list(UUID uuid) {
        return pipeline.loadAsync(
                DefaultFriend.class,
                Filters.and(
                        Filters.field("approved", true),
                        Filters.or(
                                Filters.field("sender", uuid),
                                Filters.field("target", uuid))),
                Pipeline.LoadingStrategy.LOAD_PIPELINE
        );
    }

    public CompletableFuture<List<DefaultFriend>> requests(UUID uuid) {
        return pipeline.loadAsync(
                DefaultFriend.class,
                Filters.and(
                        Filters.field("approved", false),
                        Filters.or(
                                Filters.field("sender", uuid),
                                Filters.field("target", uuid))),
                Pipeline.LoadingStrategy.LOAD_PIPELINE
        );
    }

    public boolean accept(UUID player, UUID target, Runnable callback) {
        List<DefaultFriend> result = pipeline.load(
                DefaultFriend.class,
                Filters.and(
                        Filters.field("approved", false),
                        Filters.and(
                                Filters.field("sender", target),
                                Filters.field("target", player))),
                Pipeline.LoadingStrategy.LOAD_PIPELINE
        );

        if (result.size() > 0) {
            Optional<? extends DefaultFriend> optional = Optional.ofNullable(result.get(0));

            return optional.filter(friend -> accept0(friend, callback)).isPresent();
        }

        return false;
    }

    private boolean accept0(DefaultFriend friend, Runnable callback) {
        friend.setApproved(true);
        friend.save(true, callback);
        return true;
    }

    public void acceptAll(UUID player, Runnable callback) {
        requests(player).thenAccept(friends -> {
            AtomicInteger count = new AtomicInteger();
            for (DefaultFriend friend : friends) {
                accept0(friend, () -> {
                    if (count.getAndIncrement() == friends.size()) {
                        callback.run();
                    }
                });
            }
        });
    }

    public boolean deny(UUID player, UUID target, Runnable callback) {
        List<DefaultFriend> result = pipeline.load(
                DefaultFriend.class,
                Filters.and(
                        Filters.field("approved", false),
                        Filters.or(
                                Filters.field("sender", target),
                                Filters.field("target", player))),
                Pipeline.LoadingStrategy.LOAD_PIPELINE
        );

        if (result.size() > 0) {
            Optional<? extends DefaultFriend> optional = Optional.ofNullable(result.get(0));

            return optional.filter(friend -> deny0(friend)).isPresent();
        }

        return false;
    }

    private boolean deny0(DefaultFriend friend) {
        return pipeline.delete(DefaultFriend.class, friend.getObjectUUID());
    }

    public void denyAll(UUID player) {
        requests(player).thenAccept(friends -> {
            for (DefaultFriend friend : friends) {
                deny0(friend);
            }
        });
    }

    public boolean add(UUID player, UUID target, Runnable callback) {
        DefaultFriend friend = pipeline.load(DefaultFriend.class, UUID.randomUUID(), Pipeline.LoadingStrategy.LOAD_PIPELINE, Pipeline.QueryStrategy.ALL);

        if (friend == null)
            return false;

        friend.setSender(player);
        friend.setTarget(target);
        friend.setApproved(false);

        friend.save(true, callback);
        return true;
    }

    public boolean remove(UUID player, UUID target) {
        List<DefaultFriend> result = pipeline.load(
                DefaultFriend.class,
                Filters.and(
                        Filters.field("approved", true),
                        Filters.or(
                                Filters.or(
                                        Filters.field("sender", target),
                                        Filters.field("target", player)
                                ),
                                Filters.or(
                                        Filters.field("sender", player),
                                        Filters.field("target", target)
                                ))),
                Pipeline.LoadingStrategy.LOAD_PIPELINE
        );

        if (result.size() > 0) {
            Optional<? extends DefaultFriend> optional = Optional.ofNullable(result.get(0));

            if (optional.isPresent()) {
                DefaultFriend friend = optional.get();
                return pipeline.delete(DefaultFriend.class, friend.getObjectUUID(), Pipeline.QueryStrategy.ALL);
            }
        }

        return false;
    }

}
