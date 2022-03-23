plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

defaultTasks("build", "shadowJar")

allprojects {
    group = "de.natrox"
    version = "1.1.0-SNAPSHOT"
    description = "A Java data pipeline that connects and synchronizes databases and caches, for storing Java objects"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    dependencies {
        //TEMP
        implementation(files("D:\\workspace\\NatroxMC\\Common\\build\\libs\\common.jar"))

        implementation("org.jetbrains:annotations:23.0.0")
        implementation("com.google.guava:guava:23.0")
        implementation("org.reflections:reflections:0.10.2")
        implementation("org.jodd:jodd-core:5.3.0")
        implementation("org.slf4j:slf4j-nop:1.7.36")
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
        // options
        options.encoding = "UTF-8"
        options.isIncremental = true
    }
}
