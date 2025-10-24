plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "cz.uhk.fim"
version = "1.0.2"
description = "Invoice Service"


repositories {
	mavenCentral()
}

dependencies {
	implementation(project(":common"))
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
	implementation("org.modelmapper:modelmapper:3.2.5")

	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql:42.6.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage")
	}
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
