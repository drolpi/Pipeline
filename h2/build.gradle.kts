dependencies {
    compileOnly(project(":core"))
    implementation(project(":sql"))

    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.h2database:h2:2.1.210")
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline-h2.jar")
}
