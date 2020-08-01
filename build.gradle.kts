plugins {
    id("org.jetbrains.kotlin.js") version "1.3.72"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))

    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
}

kotlin.target.nodejs { }