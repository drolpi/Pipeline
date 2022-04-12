dependencies {
    implementation(files("D:\\workspace\\NatroxMC\\Common\\build\\libs\\common.jar"))

    implementation("org.jetbrains:annotations:23.0.0")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jodd:jodd-core:5.3.0")
    implementation("com.google.code.gson:gson:2.9.0")

    testImplementation(project(":redis"))
    testImplementation(project(":mongodb"))
    testImplementation(project(":json"))
    testImplementation(project(":h2"))
    testImplementation(project(":mysql"))
    testImplementation(project(":sqlite"))
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline.jar")
}
