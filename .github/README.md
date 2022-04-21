# Pipeline
[![license](https://img.shields.io/github/license/NatroxMC/Pipeline?style=for-the-badge&color=b2204c)](../LICENSE)
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=for-the-badge)](https://github.com/RichardLitt/standard-readme)

A Java data pipeline that synchronizes Java objects between databases, caches and pipeline nodes.

# Table of contents
- [Why use the Pipeline?](#why-use-the-pipeline)
- [Advantages & Disadvantages](#advantages-and-disadvantages)
- [How to use](#how-to-use)
	- [Add to your project](#add-to-your-project)
	- [Parts](#parts)
		- [Config](#config)
		- [Custom](#custom)
		- [H2](#h2)
		- [Jsonfile](#jsonfile)
		- [MySQL](#mysql)
		- [MongoDB](#mongodb)
		- [SQLite](#sqlite)
		- [Redis](#redis)
		- [RabbitMQ](#rabbitmq-planned-feature)
		- [Netty](#netty-planned-feature)
	- [JsonProvider](#jsonprovider)
		- [Gson](#gson)
		- [Jackson](#jackson-planned-feature)
	- [DataType](#datatype)
		- [PipelineData](#pipelinedata)
		- [ConnectionData](#connectiondata)
	- [Registry](#registry)
	- [Build your Pipeline](#build-your-pipeline)
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

# How to Use
The following explains in detail how to use the pipeline and what it does.

# Add to your project
- Gradle: (Not yet available)
- Maven: (Not yet available)

# Parts
So now we can begin.
The pipeline is divided into different parts:
- [GlobalStorage](#globalstorage) also known as a database, which stores its data in persistent storage.
- [GlobalCache](#globalcache) also known as a database, which holds its data in temporary memory.
- [DataUpdater](#dataupdater) is a updater, which updates the data in the local tempory memory.
- LocalCache also know as a cache, which holds its data in the local temporary memory.

## Config
**Note: This feature is not yet implemented!**

This feature is useful if you want to write an application in which the user should be able to decide which parts he wants to use via a configuration file.

Code example:
```java
PipelineConfig config = pipelineConfigLoader.load();

PartConfig<GlobalStorageProvider> globalStorageConfig = config.globalStorage();
GlobalStorageProvider globalStorageProvider = globalStorageConfig.createProvider();

PartConfig<GlobalCacheProvider> globalCacheConfig = config.globalCache();
GlobalCacheProvider globalCacheProvider = globalCacheConfig.createProvider();

PartConfig<DataUpdaterProvider> dataUpdaterConfig = config.dataUpdater();
DataUpdaterProvider dataUpdaterProvider = dataUpdaterConfig.createProvider();
```

## Custom
Unlike the [config](#config) feature, here the developer can configure the parts himself and statically and specify which parts are to be used.

**Note:** It is also possible to mix the two features, e.g. use the GlobalStorage from the configuration and statically define that redis should be used as GlobalCache. But more about this below.

## GlobalStorage

### H2

Code example:
```java
H2Config h2Config = H2Config
    .builder()
    .path(Path.of("storage", "database"))
    .build();
H2Provider h2Provider = h2Config.createProvider();
```

### Jsonfile

Code example:
```java
JsonFileConfig jsonFileConfig = JsonFileConfig
    .builder()
    .path(Path.of("storage"))
    .build();
JsonFileProvider jsonFileProvider = jsonFileConfig.createProvider();
```

### MongoDB
#### (Single & Cluster)
MongoDB supports and has a cluster mode intigrated by default. Therefore only one MongoDB node must be specified here.

Code example:
```java
MongoConfig mongoConfig = MongoConfig
     .builder()
     .host("0.0.0.0")
     .port(27017)
     .database("database")
    .authSource("admin") //optional
    .username("username") //optional
    .password("password") //optional
    .build();
MongoProvider mongoProvider = mongoConfig.createProvider();
```
### MYSQL

#### (Single)
Code example:
```java
MySqlConfig mySqlConfig = MySqlConfig
    .builder()
    .endpoints(
        MySqlEndpoint
            .builder()
            .host("0.0.0.0")
            .port(3306)
            .database("database")
            .useSsl(false) //optional
            .build()
    )
    .username("username") //optional
    .password("password") //optional
    .build();
MySqlProvider mySqlProvider = mySqlConfig.createProvider();
```

#### (Cluster)
Code example:
```java
MySqlConfig mySqlConfig = MySqlConfig
    .builder()
    .endpoints(
        MySqlEndpoint
            .builder()
            .host("0.0.0.0")
            .port(3306)
            .database("database")
            .useSsl(false) //optional
            .build(),
        MySqlEndpoint
            .builder()
            .host("0.0.0.1")
            .port(3306)
            .database("database")
            .useSsl(false) //optional
            .build()
    )
    .username("username") //optional
    .password("password") //optional
    .build();
MySqlProvider mySqlProvider = mySqlConfig.createProvider();
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

## GlobalCache

### Redis-Cache
Click [**here**](#redis) to see how to use Redis.

## DataUpdater

### Redis-Updater
Click [**here**](#redis) to see how to use Redis.

### ~~RabbitMQ~~ (Planned feature)

### ~~Netty~~ (Planned feature)

## Redis

Redis has several part implementations. Redis can be used as a [GlobalCache](#redis-cache), as well as a [DataUpdater](#redis-updater). Or both at the same time.

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
            .database(0) //optional
            .build()
    )
    .username("username") //optional
    .password("password") //optional
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
            .database(0) //optional
            .build(),
        RedisEndpoint
            .builder()
            .host("0.0.0.1")
            .port(6379)
            .database(5) //optional
            .build()
    )
    .username("username") //optional
    .password("password") //optional
    .build();
RedisProvider redisProvider = redisConfig.createProvider();
```
# JsonProvider

The JsonProvider handles the serialization/deserialization of objects and provides a document implementation.

## Gson
Code example:
```java
JsonProvider jsonProvider = new GsonProvider();
```

## ~~Jackson~~ (Planned feature)

# DataType
There are two types of DataTypes in the pipeline.

- [PipelineData](#pipelinedata)
- [ConnectionData](#connectiondata)

## PipelineData

**Step1:** You must let your class extends from the PipelineData class and you must create a constructor matching super

Code example:
```java
public class PlayerData extends PipelineData {

    public PlayerData(@NotNull Pipeline pipeline) {
        super(pipeline);
    }
}
```
**Step 2:** You need to annotate your class with the Properties annotation. As you may have already noticed, the identifier specifies how a record should be identified in the pipeline and the conext indicates  where it should be available.

- **GLOBAL** <br>The data will be automatically loaded into the GlobalCache.

- **LOCAL** <br>The data will only be loaded into our LocalCache.

Code example:
```java
@Properties(identifier = "PlayerData", context = Context.GLOBAL)
public class PlayerData extends PipelineData {

    public PlayerData(@NotNull Pipeline pipeline) {
        super(pipeline);
    }
}
```
**Step 3:** You can add fields to your PipelineData.

### Storage fields

**Note:** Storage fields will be serialised/deserialised and will be stored therefore.

Code example:
```java
@Properties(identifier = "PlayerData", context = Context.GLOBAL)
public class PlayerData extends PipelineData {

    private String name;
    private int age;

    public PlayerData(@NotNull Pipeline pipeline) {
        super(pipeline);
    }
}
```

### Transient fields

**Note:** Transient fields will not be serialised/deserialised and will be not stored therefore.

Code example:
```java
@Properties(identifier = "PlayerData", context = Context.GLOBAL)
public class PlayerData extends PipelineData {

    private String name;
    private int age;

    private transient PlayerManager playerManager;

    public PlayerData(@NotNull Pipeline pipeline) {
        super(pipeline);
    }
}
```

### Constructor & final transient fields

**Note:** To use paramters in the constructor you must use a custom InstanceCreator. But more about this below.

Code example:
```java
@Properties(identifier = "PlayerData", context = Context.GLOBAL)
public class PlayerData extends PipelineData {

    private String name;
    private int age;

    private final transient PlayerManager playerManager;

    public PlayerData(@NotNull Pipeline pipeline, PlayerManager playerManager) {
        super(pipeline);
        this.playerManager = playerManager;
    }
}
```

**Step 4:** You can override some methods.

### OnSync
Executed after a DataUpdater synced the object.

Code example:
```java
@Override
public void onSync(DataType dataBeforeSync) {
    //code here
}
```

### OnCreate
Executed after instantiation of the object and before object is put into LocalCache.

Code example:
```java
@Override
public void onCreate() {
    //code here
}
```

### OnDelete
Executed before the object is deleted from local cache.

Code example:
```java
@Override
public void onDelete() {
    //code here
}
```

### OnLoad
Executed directly after object was loaded from pipeline. Not if it was found in LocalCache.

Code example:
```java
@Override
public void onLoad() {
    //code here
}
```

### LoadDependentData
Executed before onLoad and before onCreate everytime the data is being loaded into local cache. You can use this function to load dependent data from pipeline that is directly associated with this data.

Code example:
```java
@Override
public void loadDependentData() {
    //code here
}
```

### OnCleanUp
Executed before object is cleared from LocalCache.

Code example:
```java
@Override
public void onCleanUp() {
    //code here
}
```
**Step 5:** You can add additional annotations.

### Perload
Ensures that your PipelineData will be automatically preloaded.

Code example:
```java
@Properties(identifier = "PlayerData", context = Context.GLOBAL)
@Preload
public class PlayerData extends PipelineData {

    public PlayerData(@NotNull Pipeline pipeline) {
        super(pipeline);
    }
}
```

### AutoSave
Ensures that your PipelineData will be saved automatically.

Code example:
```java
@Properties(identifier = "PlayerData", context = Context.GLOBAL)
@AutoSave
public class PlayerData extends PipelineData {

    public PlayerData(@NotNull Pipeline pipeline) {
        super(pipeline);
    }
}
```

### CleanUp
Ensures that your PipelineData is automatically removed from the LocalCache after the specified time.

Code example:
```java
@Properties(identifier = "PlayerData", context = Context.GLOBAL)
@CleanUp(time = 10, timeUnit = ChronoUnit.MINUTES)
public class PlayerData extends PipelineData {

    public PlayerData(@NotNull Pipeline pipeline) {
        super(pipeline);
    }
}
```

## ConnectionData

The ConnectionData supports all the function that the PipelineData supports, but it adds one more function: it can be loaded and removed from a ConnectionDataLoader.

**Step 1:** You need to create a ConnectionDataLoader and call its methods.

Code example:
```java
private final ConnectionDataLoader connectionDataLoader;

public PlayerListener(Pipeline pipeline) {
    this.connectionDataLoader = new ConnectionDataLoader(pipeline);
}

public void onPlayerJoin(UUID uuid) {
    connectionDataLoader.loadConnectionData(uuid, () -> {
        //callback
    });
}

public void onPlayerLeave(UUID uuid) {
    connectionDataLoader.removeConnectionData(uuid, () -> {
        //callback
    });
}
```
**Step 2:** Furthermore, you must extend your class from the ConnectionData class and you can override some new methods.

Code example:
```java
@Properties(identifier = "PlayerData", context = Context.GLOBAL)
public class PlayerData extends ConnectionData {

    public PlayerData(@NotNull Pipeline pipeline) {
        super(pipeline);
    }

    @Override
    public void onConnect() {
        //code here
    }

    @Override
    public void onDisconnect() {
        //code here
    }
}
```

# Registry

You must register all your classes that extend from PipelineData/ConnectionData in the pipeline registry.

Code example:
```java
PipelineRegistry registry = new PipelineRegistry();

registry.register(PlayerData.class);
```

**Note: If you have added parameters to the main constructor, you need to register a custom InstanceCreator!** You can also use InstanceCreators in load methods of the pipeline.

Code example:
```java
PipelineRegistry registry = new PipelineRegistry();
PlayerManager playerManager; //a example class

registry.register(PlayerData.class, (dataClass, pipeline) -> new PlayerData(pipeline, playerManager));
```

# Build your Pipeline
Now the time has finally come, you can start building your pipeline.

Code example:
```java
PipelineRegistry registry; //the PipelineRegistry you created in the "Registry" section.
JsonProvider jsonProvider; //the JsonProvider you created in the "JsonProvider" section.
MongoProvider mongoProvider; //the MongoProvider you created in the "MongoDB" section.
GlobalCacheProvider globalCacheProvider; //the GlobalCacheProvider your created in the "Config" section.
RedisProvider redisProvider; //the RedisProvider you created in the "Redis" section.

Pipeline pipeline = Pipeline
    .builder()
    .registry(registry)
    .jsonProvider(jsonProvider)
    .globalStorage(mongoProvider) //optional
    .globalCache(globalCacheProvider) //optional
    .dataUpdater(redisProvider) //optional
    .build();
```

# Contributing
See [the contributing file](CONTRIBUTING.md)!
All WIP features are previewed as Draft PRs

# License
This project is licensed under the [Apache License Version 2.0](../LICENSE).
