import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

plugins {
	id("org.springframework.boot") version "4.1.0" apply false
	id("io.spring.dependency-management") version "1.1.7" apply false
}

group = "com.sleekydz86"
version = "0.0.1-SNAPSHOT"
description = "LoginStudy Identity Platform"

allprojects {
	group = rootProject.group
	version = rootProject.version

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")

	configure<JavaPluginExtension> {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(21))
		}
	}

	tasks.withType<Test>().configureEach {
		useJUnitPlatform()
		maxHeapSize = "512m"
		jvmArgs("-XX:MaxMetaspaceSize=192m")
	}

	tasks.withType<JavaCompile>().configureEach {
		options.encoding = "UTF-8"
	}
}
