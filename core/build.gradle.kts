dependencies {
    implementation(files("D:\\workspace\\NatroxMC\\Common\\build\\libs\\common.jar"))

    implementation("org.jetbrains:annotations:23.0.0")
    implementation("com.google.guava:guava:23.0")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jodd:jodd-core:5.3.0")
    implementation("org.slf4j:slf4j-nop:1.7.36")

    testImplementation(project(":mongodb"))
    testImplementation(project(":redis"))
}

tasks.withType<Jar> {
    archiveFileName.set("pipeline.jar")
}
