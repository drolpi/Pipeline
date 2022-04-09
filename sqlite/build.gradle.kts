dependencies {
    compileOnly(project(":core"))
    implementation(project(":sql"))

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline-sql-lite.jar")
}
