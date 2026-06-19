rootProject.name = "kanban"

pluginManagement {
    plugins {
        kotlin("jvm") version "2.0.21"
        kotlin("plugin.spring") version "2.0.21"
        kotlin("plugin.serialization") version "2.0.21"
        id("org.springframework.boot") version "3.3.5"
        id("io.spring.dependency-management") version "1.1.6"
        id("org.graalvm.buildtools.native") version "0.10.3"
        id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
        id("io.gitlab.arturbosch.detekt") version "1.23.8"
    }
}

include(":spring", ":usecase", ":webapi", ":postgres", ":nats")
