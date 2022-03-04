plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "de.notion"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    dependencies {
        implementation(files("D:\\NotionPowered\\projects\\notion-common\\build\\libs\\notion-common.jar"))

        implementation("org.jetbrains:annotations:23.0.0")
        implementation("com.google.guava:guava:23.0")
        implementation("org.reflections:reflections:0.10.2")
        implementation("org.jodd:jodd-core:5.3.0")
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }

        shadowJar {
            //Set the Name of the Output File
            archiveFileName.set("${rootProject.name}-${project.name}.jar")
        }
    }
}