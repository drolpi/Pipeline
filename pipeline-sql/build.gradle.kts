dependencies {
    compileOnly(project(":pipeline-core"))
    implementation("com.zaxxer:HikariCP:3.3.1")
    implementation("mysql:mysql-connector-java:8.0.28")
    implementation("com.h2database:h2:1.4.199")
}
