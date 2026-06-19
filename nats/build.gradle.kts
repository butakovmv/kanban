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
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation(kotlin("test"))
}
