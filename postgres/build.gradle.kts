plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.5"))
    implementation(project(":usecase"))
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.flywaydb:flyway-core")
    implementation("org.postgresql:r2dbc-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("io.r2dbc:r2dbc-h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
}
