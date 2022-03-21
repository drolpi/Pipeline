dependencies {
    compileOnly(project(":core"))
    implementation(project(":sql"))

    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline-mysql.jar")
}
