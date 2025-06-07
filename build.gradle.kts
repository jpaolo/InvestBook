plugins {
    kotlin("jvm") version "2.1.20"
}

group = "org.home.prac.invest.book"
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