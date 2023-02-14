plugins {
    id("queryfingerprint.java-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":snowflake"))
    implementation(files("$projectDir/../lib/gudusoft.gsqlparser-2.6.1.1.jar"))
    implementation("co.elastic.clients:elasticsearch-java:8.6.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.3")
}
