# Pipeline
A Java data pipeline that synchronizes Java objects between databases, caches and pipeline nodes

# Table of contents
- How to use
	- [Add to your project](#add-to-your-project)
	- [Parts](#parts)
		- [Config](#config)
		- [H2](#h2-local)
		- [Jsonfile](#jsonfile)
		- [MySQL](#mysql)
		- [MongoDB](#mongodb)
		- [SQLite](#sqlite)
	- [Registry](#registry)
	- [JsonProvider](#jsonprovider)
	- [Build your Pipeline](#build-your-pipeline)

# Add to your project
Gradle:
Maven:

# Parts
Description

## Config
Description
```java
PipelineConfig config = pipelineConfigLoader.load();

PartConfig<GlobalStorageProvider> globalStorageConfig = config.globalStorage();
GlobalStorageProvider globalStorageProvider = globalStorageConfig.createProvider();

PartConfig<GlobalCacheProvider> globalCacheConfig = config.globalCache();
GlobalCacheProvider globalCacheProvider = globalCacheConfig.createProvider();

PartConfig<DataUpdaterProvider> dataUpdaterConfig = config.dataUpdater();
DataUpdaterProvider dataUpdaterProvider = dataUpdaterConfig.createProvider();
```

## GlobalStorage
Description

### H2
Description

### Jsonfile
Description
```java
JsonFileConfig jsonFileConfig = JsonFileConfig
    .builder()
    .path(Path.of("storage"))
    .build();
JsonFileProvider jsonFileProvider = jsonFileConfig.createProvider();
```

### MongoDB
#### (Single & Cluster)
MongoDB supports and has a cluster mode intigrated by default. Therefore only one MongoDB node must be specified here
```java
MongoConfig mongoConfig = MongoConfig
     .builder()
     .host("0.0.0.0")
     .port(27017)
     .database("database")
    .build();
MongoProvider mongoProvider = mongoConfig.createProvider();
```
### MYSQL
#### (Single)
Description
```java
MySqlConfig mySqlConfig = MySqlConfig
    .builder()
    .endpoints(
        MySqlEndpoint
            .builder()
            .host("0.0.0.0")
            .port(3306)
            .database("database")
            .useSsl(false)
            .build()
    )
    .username("username")
    .password("password")
    .build();
MySqlProvider mySqlProvider = mySqlConfig.createProvider();
```

#### (Cluster)
Description
```java
MySqlConfig mySqlConfig = MySqlConfig
    .builder()
    .endpoints(
        MySqlEndpoint
            .builder()
            .host("0.0.0.0")
            .port(3306)
            .database("database")
            .useSsl(false)
            .build(),
        MySqlEndpoint
            .builder()
            .host("0.0.0.1")
            .port(3306)
            .database("database")
            .useSsl(false)
            .build()
    )
    .username("username")
    .password("password")
    .build();
MySqlProvider mySqlProvider = mySqlConfig.createProvider();
```

### SQLite
Description

## GlobalCache
Description

### Redis
#### (Single)
Description

#### (Cluster)
Description

## DataUpdater
Description

### Redis
#### (Single)
Description

#### (Cluster)
Description

# Registry
Description

# JsonProvider
Description

# Build your Pipeline
Description

```java
Pipeline pipeline = Pipeline
    .builder()
    .registry(registry) #the PipelineRegistry you created above
    .jsonProvider(jsonProvider) #the JsonProvider you created above
    .globalStorage(mongoProvider) #the MongoProvider you created above
    .globalCache(globalCacheProvider) #the GlobalCacheProvider you created above
    .dataUpdater(redisProvider) #the RedisProvider your created above
    .build();
```
