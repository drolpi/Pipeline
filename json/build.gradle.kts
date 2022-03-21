dependencies {
    compileOnly(project(":core"))
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline-json.jar")
}
