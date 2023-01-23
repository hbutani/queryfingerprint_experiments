plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    `java-library`
    `maven-publish`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()

}

group = "org.hatke"
version = "0.0.1"

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:31.0.1-jre")

    api("org.apache.logging.log4j:log4j-api:2.13.3")
    api("org.apache.logging.log4j:log4j-core:2.13.3")
    api("org.apache.logging.log4j:log4j-slf4j-impl:2.13.3")
    api("org.slf4j:slf4j-api:2.0.6")
    api("org.slf4j:slf4j-log4j12:2.0.6")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.compileJava {
    options.isIncremental = true
    options.isFork = true
    // options.isFailOnError = false
}