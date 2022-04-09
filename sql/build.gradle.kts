dependencies {
    compileOnly(project(":core"))

    implementation("com.zaxxer:HikariCP:5.0.1")
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline-sql.jar")
}
