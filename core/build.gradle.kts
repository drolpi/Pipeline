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

dependencies {
    implementation("com.github.NatroxMC:common:7c25cf3135")
    implementation("com.github.NatroxMC:eventbus:8cd653bd63")
    implementation("com.github.NatroxMC:conversionbus:84ad8b61dc")

    implementation("org.jetbrains:annotations:23.0.0")
    implementation("org.jodd:jodd-core:5.3.0")
    implementation("com.esotericsoftware.kryo:kryo5:5.3.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testImplementation("org.junit.platform:junit-platform-suite-api:1.9.0")
    testRuntimeOnly("org.junit.platform:junit-platform-suite-engine:1.9.0")
    testImplementation("org.mockito:mockito-core:4.6.1")
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline.jar")
}
