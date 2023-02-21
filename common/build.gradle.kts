plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    // `java-library`
    id("queryfingerprint.java-conventions")
    `scala`
}

dependencies {
    implementation("org.scala-lang:scala-library:2.13.10")

    implementation("com.typesafe:config:1.4.1")

    testImplementation("org.scalatest:scalatest_2.13:3.2.13")
    testImplementation("org.scalatestplus:junit-4-13_2.13:3.2.2.0")
}

scala {

}

tasks {
    compileScala {
        targetCompatibility = ""
    }
}