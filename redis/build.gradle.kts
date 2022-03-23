dependencies {
    compileOnly(project(":core"))
    implementation("org.redisson:redisson-all:3.17.0")
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline-redis.jar")
}
