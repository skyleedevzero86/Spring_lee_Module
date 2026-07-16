plugins {
	`java-library`
	id("io.spring.dependency-management")
}

dependencyManagement {
	imports {
		mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
	}
}

dependencies {
	api(project(":event-contract"))
	implementation("org.springframework.boot:spring-boot-starter-test")
}
