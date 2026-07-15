package com.sleekydz86.loginstudy.auth;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

abstract class AuthServerIntegrationTestSupport {

	private static final Path PRIVATE_KEY_PATH;
	private static final Path PUBLIC_KEY_PATH;
	private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
			.withExposedPorts(6379);

	static {
		try {
			REDIS.start();

			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			KeyPair keyPair = generator.generateKeyPair();

			PRIVATE_KEY_PATH = Files.createTempFile("loginstudy-auth-private-", ".pem");
			PUBLIC_KEY_PATH = Files.createTempFile("loginstudy-auth-public-", ".pem");
			PRIVATE_KEY_PATH.toFile().deleteOnExit();
			PUBLIC_KEY_PATH.toFile().deleteOnExit();

			Files.writeString(PRIVATE_KEY_PATH, toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
			Files.writeString(PUBLIC_KEY_PATH, toPem("PUBLIC KEY", keyPair.getPublic().getEncoded()));
		}
		catch (Exception ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@DynamicPropertySource
	static void registerTestProperties(DynamicPropertyRegistry registry) {
		registry.add("auth.rsa.private-key-location", () -> PRIVATE_KEY_PATH.toUri().toString());
		registry.add("auth.rsa.public-key-location", () -> PUBLIC_KEY_PATH.toUri().toString());
		registry.add("auth.rsa.key-id", () -> "test-key");
		registry.add("spring.security.oauth2.authorizationserver.issuer", () -> "http://localhost:9000");
		registry.add("spring.data.redis.host", REDIS::getHost);
		registry.add("spring.data.redis.port", () -> String.valueOf(REDIS.getMappedPort(6379)));
	}

	private static String toPem(String type, byte[] encoded) {
		String base64 = Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(encoded);
		return "-----BEGIN " + type + "-----\n" + base64 + "\n-----END " + type + "-----\n";
	}
}
