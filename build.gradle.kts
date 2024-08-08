plugins {
    kotlin("jvm") version "1.9.23"
}

group = "me.dolphin2410"
version = "1.0.0"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    implementation("io.github.monun:tap-api:4.9.8")
    implementation("io.github.monun:kommand-api:3.1.7")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}