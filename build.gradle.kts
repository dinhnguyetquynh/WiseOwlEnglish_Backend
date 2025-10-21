plugins {
	java
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.iuh"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
//	runtimeOnly("com.mysql:mysql-connector-j")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	//POSTGRES DUNG DE DEMO DO AN//
//	 https://mvnrepository.com/artifact/org.postgresql/postgresql
		implementation("org.postgresql:postgresql:42.7.7")

	//MARIADB CHI DUNG DE TEST LOCAL//
	// https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client
//	implementation("org.mariadb.jdbc:mariadb-java-client:3.5.6")

	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

	implementation("org.springframework.boot:spring-boot-starter-validation")

	// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-mail
	implementation("org.springframework.boot:spring-boot-starter-mail:3.5.5")

	implementation("com.cloudinary:cloudinary-http44:1.38.0")

	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5") // dùng Jackson parse JSON


}

tasks.withType<Test> {
	useJUnitPlatform()
}
