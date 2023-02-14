plugins {
    id("queryfingerprint.java-conventions")
}

dependencies {
    // based on https://stackoverflow.com/questions/54166069/how-do-you-add-local-jar-file-dependency-to-build-gradle-kt-file
    implementation(files("$projectDir/../lib/gudusoft.gsqlparser-2.6.1.1.jar"))

    implementation(project(":common"))
}

