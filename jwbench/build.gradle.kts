import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("me.champeau.jmh") version "0.6.8"
    id("queryfingerprint.java-conventions")
    kotlin("jvm") version "1.8.20-Beta"
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    ->
    implementation(files("$projectDir/../lib/gudusoft.gsqlparser-2.6.1.1.jar"))
    implementation(project(":snowflake"))
    implementation(project(mapOf("path" to ":common")))
    implementation(project(mapOf("path" to ":snowflake")))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation("org.openjdk.jmh:jmh-core:1.35")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:1.35")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.35")
    implementation(kotlin("stdlib-jdk8"))
    runtimeOnly("jakarta.xml.bind:jakarta.xml.bind-api:2.3.2")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime:2.3.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}


val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}