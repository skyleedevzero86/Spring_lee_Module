pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "catalogflow"

include(
	"catalog-domain",
	"event-contract",
	"catalog-security",
	"test-support",
	"catalog-command-service",
	"catalog-query-service",
	"ai-enrichment-worker",
	"catalog-batch-service"
)
