# Pipeline (NOT READY FOR PRODUCTION)

[![license](https://img.shields.io/github/license/NatroxMC/Pipeline?style=for-the-badge&color=b2204c)](../LICENSE)
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=for-the-badge)](https://github.com/RichardLitt/standard-readme)

Pipeline is a very fast and open-source store library that synchronizes records between databases and caches. It supports all types of databases and caches and already has many implemented. It has a fast and easy-to-use API.

# Table of contents

- [Why use the Pipeline?](#why-use-the-pipeline)
- [Advantages & Disadvantages](#advantages-and-disadvantages)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

# Why use the Pipeline

Coming soon.

# Advantages and Disadvantages

Coming soon.

## Advantages

- Coming soon
- Coming soon

## Disadvantages

- Coming soon
- Coming soon

# Usage
The following explains in detail how to use the pipeline and what it does.

# Add to your project
- Gradle: (Not yet available)
- Maven: (Not yet available)

# Parts
So now we can begin. The pipeline is divided into different parts:
- [Storage](#storage) also known as a persistent data store.
  - [GlobalStorage](#globalstorage) is a storage implementation, which stores its data in a global database.
  - [LocalStorage](#localstorage) is a storage implementation that stores its data locally, e.g. file-based.
- [Cache](#cache) also knwo as a temporary data store.
  - [GlobalCache](#globalcache) is a cache implementation, which holds its data in a global temporary memory store.
  - [LocalCache](#localcache) is a cache implementation, which holds its data in the local temporary memory.
- [DataUpdater](#dataupdater) is an updater, which updates the the local tempory memory store.

## Config
**Note: This feature is not yet implemented!**

This feature is useful if you want to write an application in which the user should be able to decide which parts he wants to use via a configuration file.
```java
Not.ready().yet();
```
## Custom
Unlike the [config](#config) feature, here the developer can configure the parts himself and statically and specify which parts are to be used.

**Note:** It is also possible to mix the two features, e.g. use the GlobalStorage from the configuration and statically define that redis should be used as GlobalCache. But more about this [below](#build-your-pipeline).

## Storage
A persistent data store.

## GlobalStorage
A global persistent data store.

### MongoDB
#### (Single & Cluster)

Code example:
```java
MongoConfig mongoConfig = MongoConfig
    .builder()
    .host("0.0.0.0")
    .port(27017)
    .database("database")
    .authSource("admin") // Optional
    .username("username") // Optional
    .password("password") // Optional
    .build();
MongoProvider mongoProvider = mongoConfig.createProvider();
```

### MYSQL
#### (Single & Cluster)

Code example:
```java
MySqlConfig mySqlConfig = MySqlConfig
    .builder()
    .host("0.0.0.0")
    .port(3306)
    .database("database")
    .useSsl(false) //Optional
    .username("username") // Optional
    .password("password") // Optional
    .build();
MySqlProvider mySqlProvider = mySqlConfig.createProvider();
```

## LocalStorage
A local persistent data store.

### H2

Code example:
```java
H2Config h2Config = H2Config
    .builder()
    .path(Path.of("storage", "database"))
    .build();
H2Provider h2Provider = h2Config.createProvider();
```

### Json

Code example:
```java
JsonConfig jsonFileConfig = JsonConfig
    .builder()
    .path(Path.of("storage"))
    .build();
JsonProvider jsonProvider = jsonConfig.createProvider();
```

### SQLite
Code example:
```java
SQLiteConfig sqLiteConfig = SQLiteConfig
    .builder()
    .path(Path.of("storage", "datbase.sqlite"))
    .build();
SQLiteProvider sqLiteProvider = sqLiteConfig.createProvider();
```
## Cache
A temporary data store.

## GlobalCache
A global temporary data store.

### Redis-Cache
#### (Single)
Code example:
```java
RedisConfig redisConfig = RedisConfig
    .builder()
    .endpoints(
        RedisEndpoint
            .builder()
            .host("0.0.0.0")
            .port(6379)
            .database(0) // Optional
            .build()
    )
    .username("username") // Optional
    .password("password") // Optional
    .build();
RedisProvider redisProvider = redisConfig.createProvider();
```

#### (Cluster)
Code example:
```java
RedisConfig redisConfig = RedisConfig
    .builder()
    .endpoints(
        RedisEndpoint
            .builder()
            .host("0.0.0.0")
            .port(6379)
            .database(0) // Optional
            .build(),
        RedisEndpoint
            .builder()
            .host("0.0.0.1")
            .port(6379)
            .database(5) // Optional
            .build()
    )
    .username("username") // Optional
    .password("password") // Optional
    .build();
RedisProvider redisProvider = redisConfig.createProvider();
```

## LocalCache
A local temporary data store.

## InMemory-Cache

## DataUpdater

### Redis-Updater
#### (Single)
Code example:
```java
RedisConfig redisConfig = RedisConfig
    .builder()
    .endpoints(
        RedisEndpoint
            .builder()
            .host("0.0.0.0")
            .port(6379)
            .database(0) // Optional
            .build()
    )
    .username("username") // Optional
    .password("password") // Optional
    .build();
RedisProvider redisProvider = redisConfig.createProvider();
```

#### (Cluster)
Code example:
```java
RedisConfig redisConfig = RedisConfig
    .builder()
    .endpoints(
        RedisEndpoint
            .builder()
            .host("0.0.0.0")
            .port(6379)
            .database(0) // Optional
            .build(),
        RedisEndpoint
            .builder()
            .host("0.0.0.1")
            .port(6379)
            .database(5) // Optional
            .build()
    )
    .username("username") // Optional
    .password("password") // Optional
    .build();
RedisProvider redisProvider = redisConfig.createProvider();
```

### ~~RabbitMQ~~ (Planned feature)

### ~~Netty~~ (Planned feature)

# Mapper

The DocumentMapper handles the serialization/deserialization of the DocumentData class.

## Gson

Code example:
```java
DocumentMapper documentMapper = GsonDocumentMapper.create();
```

## Jackson

Code example:
```java
DocumentMapper documentMapper = JacksonDocumentMapper.create();
```

# Build your Pipeline
Now the time has finally come, you can start building your pipeline.

TODO:

# Contributing

See [the contributing file](CONTRIBUTING.md)!
All WIP features are previewed as Draft PRs

# License

This project is licensed under the [Apache License Version 2.0](../LICENSE).
