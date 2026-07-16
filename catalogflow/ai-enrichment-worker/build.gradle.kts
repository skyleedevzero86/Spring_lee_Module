plugins {
	java
	id("org.springframework.boot")
	id("io.spring.dependency-management")
}

dependencies {
	implementation(project(":event-contract"))

	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-json")

	implementation(platform("org.springframework.ai:spring-ai-bom:2.0.0"))
	implementation("org.springframework.ai:spring-ai-ollama")
	implementation("org.springframework.ai:spring-ai-google-genai")
	implementation("org.springframework.ai:spring-ai-client-chat")

	testImplementation(project(":test-support"))
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-rabbitmq")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:2.0.0")
	}
}
