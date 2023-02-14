plugins {
    id("queryfingerprint.java-conventions")
    `scala`
}

dependencies {
    implementation("org.scala-lang:scala-library:2.13.10")

    testImplementation("org.scalatest:scalatest_2.13:3.2.13")
    testImplementation("org.scalatestplus:junit-4-13_2.13:3.2.2.0")

    implementation(project(":common"))
    implementation(project(":snowflake"))
    implementation(files("$projectDir/../lib/gudusoft.gsqlparser-2.6.1.1.jar"))

    implementation("com.sksamuel.elastic4s:elastic4s-client-esjava_2.13:8.5.2")
    implementation("com.sksamuel.elastic4s:elastic4s-json-json4s_2.13:8.5.2")
    testImplementation("com.sksamuel.elastic4s:elastic4s-testkit_2.13:8.5.2")

    testImplementation("org.testcontainers:elasticsearch:1.17.6")

    implementation("org.json4s:json4s-ext_2.13:4.1.0-M2")
    implementation("org.json4s:json4s-jackson_2.13:4.1.0-M2")

    testImplementation(project(":snowflake"))
    testImplementation(files("$projectDir/../lib/gudusoft.gsqlparser-2.6.1.1.jar"))

}

scala {

}

tasks {
    compileScala {
        targetCompatibility = ""
    }
}
