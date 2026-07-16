plugins {
	id("org.springframework.boot") version "4.1.0" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
	group = "com.sleekydz86"
	version = "0.0.1-SNAPSHOT"
	description = "CatalogFlow AI"

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "java")

	extensions.configure<JavaPluginExtension> {
		toolchain {
			languageVersion = JavaLanguageVersion.of(21)
		}
	}

	tasks.withType<JavaCompile>().configureEach {
		options.encoding = "UTF-8"
		options.compilerArgs.add("-parameters")
	}

	tasks.withType<Test>().configureEach {
		useJUnitPlatform()
	}
}
