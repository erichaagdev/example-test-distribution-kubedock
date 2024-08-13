plugins {
    id("java")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val test by testing.suites.getting(JvmTestSuite::class) {
    useJUnitJupiter()
    dependencies {
        implementation("org.testcontainers:postgresql:1.20.0")
        implementation("org.postgresql:postgresql:42.7.3")
    }
    targets.configureEach {
        testTask {
            outputs.upToDateWhen { false }
            develocity {
                testDistribution {
                    enabled = true
                    maxLocalExecutors = providers.gradleProperty("maxLocalExecutors").map { it.toInt() }.orElse(0)
                    maxRemoteExecutors = providers.gradleProperty("maxRemoteExecutors").map { it.toInt() }.orElse(0)
                }
            }
        }
    }
}
