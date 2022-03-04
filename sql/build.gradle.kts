dependencies {
    compileOnly(project(":core"))
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("mysql:mysql-connector-java:8.0.28")
    implementation("com.h2database:h2:2.1.210")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
}
