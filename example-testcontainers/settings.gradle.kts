plugins {
    id("com.gradle.develocity") version "3.17.6"
}

rootProject.name = "example-testcontainers"

develocity {
    server = providers.environmentVariable("DEVELOCITY_SERVER_URL")
}
