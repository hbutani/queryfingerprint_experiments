plugins {
    id("queryfingerprint.java-conventions")
    `scala`
}

dependencies {
    implementation("org.scala-lang:scala-library:2.13.10")

    testImplementation("org.scalatest:scalatest_2.13:3.2.13")
    testImplementation("org.scalatestplus:junit-4-13_2.13:3.2.2.0")

    implementation(project(":common"))

    implementation("com.sksamuel.elastic4s:elastic4s-client-esjava_2.13:8.5.2")
    testImplementation("com.sksamuel.elastic4s:elastic4s-testkit_2.13:8.5.2")

    testImplementation("org.testcontainers:elasticsearch:1.17.6")
}

scala {

}

tasks {
    compileScala {
        targetCompatibility = ""
    }
}
