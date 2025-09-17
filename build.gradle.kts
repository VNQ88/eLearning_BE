plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"

    val kotlinVersion = "1.9.22"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

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
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // https://mvnrepository.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")

    kapt("org.mapstruct:mapstruct-processor:1.5.5.Final")
    implementation("io.minio:minio:8.5.17")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    implementation("me.paulschwarz:spring-dotenv:4.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("org.springframework.data:spring-data-redis")
    implementation("redis.clients:jedis")
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-mail
    implementation("org.springframework.boot:spring-boot-starter-mail:3.5.3")
}

kapt {
    keepJavacAnnotationProcessors = true
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
        arg("mapstruct.unmappedTargetPolicy", "IGNORE")
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
}
