plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "cz.uhk.fim"
version = "1.0.3"
description = "Invoice Service"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("org.modelmapper:modelmapper:3.2.5")
    runtimeOnly("com.h2database:h2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}


kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
