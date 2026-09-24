plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "searchai"

include("proto", "ai-service", "gateway", "notification")
