dependencies {
    testImplementation(project(":mongodb"))
    testImplementation(project(":redis"))
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline.jar")
}
