# Pipeline [WIP - Not usable]

[![license](https://img.shields.io/github/license/NatroxMC/Pipeline?style=for-the-badge&color=b2204c)](../LICENSE)
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=for-the-badge)](https://github.com/RichardLitt/standard-readme)
[![discord-banner](https://shields.io/discord/718476275022299157?label=discord&style=for-the-badge&color=7289da)](https://discord.natrox.de)

Pipeline is a very fast and open-source store library that synchronizes records between databases and caches. It
supports all types of databases and caches and already has many implemented. It has a fast and easy-to-use API.

# Table of contents
- [Usage](#usage)
- [Why is Pipeline not usable yet?](#why-is-pipeline-not-usable-yet)
- [Why Pipeline?](#why-pipeline)
- [Advantages & Disadvantages](#advantages-and-disadvantages)
- [API](#api)
- [Contributing](#contributing)
- [License](#license)

# Usage
An example of how to use the Pipeline library is available [here](/demo).

# Why is Pipeline not usable yet?
The Pipeline is already very far but not done yet. 

Those things are missing to make it usable:
* Improvements
* JUnit Tests

# Why Pipeline?
Coming soon

# Advantages and Disadvantages
The Pipeline library isn't perfect, our choices make it much better for some cases, worse for some others.

## Advantages
* Coming soon
* Coming soon

## Disadvantages
* Coming soon
* Coming soon

# API
Adding the Pipeline library to your project is really simple, you only need to add a few repositories. The Pipeline library needs Java 17 or newer. If you are using Gradle, you must use version 7.2 or higher.

## Gradle

#### Repository
```java
repositories {
    mavenCentral()
    maven("https://repo.natrox.de")
}
```

#### Dependency
```java
dependencies {
    implementation("de.natrox:pipeline-core:LATEST")
}
```
## Maven

#### Repository
```xml
<repositories>
    <repository>
        <id>natrox</id>
        <url>https://repo.natrox.de</url>
    </repository>
</repositories>
```

#### Dependency
```xml
<dependencies>
    <dependency>
        <groupId>de.natrox</groupId>
        <artifactId>pipeline-core</artifactId>
        <version>LATEST</version>
    </dependency>
</dependencies>
```

# Contributing
See [the contributing file](CONTRIBUTING.md)!
All WIP features are previewed as Draft PRs!

# License
This project is licensed under the [Apache License Version 2.0](../LICENSE).
