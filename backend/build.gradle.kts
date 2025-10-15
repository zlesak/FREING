plugins {
	kotlin("jvm") version "1.9.25" apply false
	kotlin("plugin.spring") version "1.9.25" apply false
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"


subprojects {
	apply(plugin = "io.spring.dependency-management")
	apply(plugin = "kotlin")

	repositories {
		mavenCentral()
	}


//	dependencies {
//		testImplementation("org.springframework.boot:spring-boot-starter-test")
//		testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
//		testRuntimeOnly("org.junit.platform:junit-platform-launcher")
//	}
}
