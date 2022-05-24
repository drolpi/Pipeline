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
    implementation(files("D:\\workspace\\NatroxMC\\Common\\build\\libs\\common.jar"))
    implementation(files("D:\\workspace\\NatroxMC\\EventBus\\build\\libs\\eventbus.jar"))

    implementation("org.jetbrains:annotations:23.0.0")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jodd:jodd-core:5.3.0")

    testImplementation(project(":mongo"))
    testImplementation(project(":redis"))
    testImplementation(project(":gson-mapper"))
    testImplementation(project(":jackson-mapper"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testImplementation("org.junit.platform:junit-platform-suite-api:1.8.2")
    testRuntimeOnly("org.junit.platform:junit-platform-suite-engine:1.8.2")
    testImplementation("org.mockito:mockito-core:4.5.1")
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline.jar")
}
