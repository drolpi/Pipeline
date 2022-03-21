dependencies {
    compileOnly(project(":core"))
    implementation("org.redisson:redisson-all:3.16.8")
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline-redis.jar")
}