plugins {
    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
    kotlin("plugin.serialization") apply false
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
}

group = "com.kanban"
version = "0.1.0-SNAPSHOT"

subprojects {
    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        options.release.set(21)
    }
}
