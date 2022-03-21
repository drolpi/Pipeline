dependencies {
    testImplementation(project(":mongodb"))
    testImplementation(project(":sql"))
    testImplementation(project(":redis"))
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline.jar")
}