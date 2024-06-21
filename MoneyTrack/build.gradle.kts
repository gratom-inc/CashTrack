plugins {
    kotlin("jvm") version "1.9.23"
}

group = "com.gratom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.apache.commons/commons-csv
    implementation("org.apache.commons:commons-csv:1.11.0")

    implementation("io.github.rtmigo:dec:0.1.8")
    implementation("com.github.labai:deci:0.0.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
