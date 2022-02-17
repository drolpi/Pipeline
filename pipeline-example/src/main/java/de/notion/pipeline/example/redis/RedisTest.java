package de.notion.pipeline.example.redis;

import de.notion.pipeline.redis.RedisConfig;

public class RedisTest {

    public static void main(String[] args) {
        RedisConfig redisConfig = new RedisConfig(false, "", "redis://127.0.0.1:6379");
        redisConfig.load();
    }

}
