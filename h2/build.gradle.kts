dependencies {
    compileOnly(project(":core"))
    implementation(project(":sql"))

    implementation("com.h2database:h2:2.1.210")
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline-mysql.jar")
}
