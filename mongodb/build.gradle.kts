dependencies{
    compileOnly(project(":core"))
    implementation("org.mongodb:mongo-java-driver:3.12.10")
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline-mongodb.jar")
}
