# Pipeline [WIP - Not usable]

[![license](https://img.shields.io/github/license/NatroxMC/Pipeline?style=for-the-badge&color=b2204c)](../LICENSE)
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=for-the-badge)](https://github.com/RichardLitt/standard-readme)
[![discord-banner](https://shields.io/discord/718476275022299157?label=discord&style=for-the-badge&color=7289da)](https://discord.natrox.de)

Pipeline is a very fast and open-source store library that synchronizes records between databases and caches. It
supports all types of databases and caches and already has many implemented. It has a fast and easy-to-use API.

# Table of contents
- [Usage](#usage)
- [Why not usable yet?](#why-not-usable-yet)
- [Advantages & Disadvantages](#advantages-and-disadvantages)
- [API](#api)
- [Contributing](#contributing)
- [License](#license)

# Usage
An example of how to use this library is available [here](/demo).

# Why not usable yet?
This library is already very far but not done yet. 

Those things are missing to make it usable:
* Improvements
* JUnit Tests

# Advantages and Disadvantages
This library isn't perfect, our choices make it much better for some cases, worse for some others.

## Advantages
* Coming soon
* Coming soon

## Disadvantages
* Coming soon
* Coming soon

# API
Gradle:
```java
repositories {
    maven("https://repo.natrox.de/repository/maven-public/")
}

dependencies {
    implementation("de.natrox:pipeline:VERSION")
}
```
Maven:
```xml
<repositories>
    <repository>
        <id>natrox</id>
        <url>https://repo.natrox.de/repository/maven-public/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>de.natrox</groupId>
        <artifactId>pipeline</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```

# Contributing
See [the contributing file](CONTRIBUTING.md)!
All WIP features are previewed as Draft PRs!

# License
This project is licensed under the [Apache License Version 2.0](../LICENSE).
