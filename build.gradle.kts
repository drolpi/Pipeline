/*
 * Copyright 2020-2022 NatroxMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

defaultTasks("build", "shadowJar")

allprojects {
    group = "de.natrox"
    version = "2.0.0-SNAPSHOT"
    description = "A fast and lightweight store that synchronizes records between databases and caches"

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
        compileOnly(files("D:\\workspace\\NatroxMC\\Common\\build\\libs\\common.jar"))
        compileOnly(files("D:\\workspace\\NatroxMC\\Eventbus\\build\\libs\\eventbus.jar"))

        compileOnly("org.jetbrains:annotations:23.1.0")
        compileOnly("org.reflections:reflections:0.10.2")
        compileOnly("org.jodd:jodd-core:5.3.0")
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
        // options
        options.encoding = "UTF-8"
        options.isIncremental = true
    }
}
