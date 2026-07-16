plugins {
	java
	id("org.springframework.boot")
	id("io.spring.dependency-management")
}

dependencies {
	implementation(project(":catalog-domain"))
	implementation(project(":event-contract"))

	implementation("org.springframework.boot:spring-boot-starter")

	testImplementation(project(":test-support"))
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
