plugins {
    kotlin("jvm") version "1.9.23"
}

group = "io.github.zyrouge.symphony"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(19)
}