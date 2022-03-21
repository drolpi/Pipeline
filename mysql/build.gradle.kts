dependencies {
    compileOnly(project(":core"))
    implementation(project(":sql"))

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("mysql:mysql-connector-java:8.0.28")
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline-mysql.jar")
}
