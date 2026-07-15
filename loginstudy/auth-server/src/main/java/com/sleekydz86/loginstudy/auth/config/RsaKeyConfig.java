package com.sleekydz86.loginstudy.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@Configuration
@EnableConfigurationProperties(RsaKeyProperties.class)
public class RsaKeyConfig {

	@Bean
	JWKSource<SecurityContext> jwkSource(RsaKeyProperties properties, ResourceLoader resourceLoader) {
		Resource publicKeyResource = resolveExisting(resourceLoader, properties.getPublicKeyLocation(), "public.pem");
		Resource privateKeyResource = resolveExisting(resourceLoader, properties.getPrivateKeyLocation(), "private.pem");

		if (publicKeyResource == null || privateKeyResource == null) {
			Path keyDirectory = resolveWritableKeyDirectory(properties);
			generateAndStore(keyDirectory);
			publicKeyResource = resourceLoader.getResource(keyDirectory.resolve("public.pem").toUri().toString());
			privateKeyResource = resourceLoader.getResource(keyDirectory.resolve("private.pem").toUri().toString());
		}

		RSAPublicKey publicKey = readPublicKey(publicKeyResource);
		RSAPrivateKey privateKey = readPrivateKey(privateKeyResource);

		RSAKey rsaKey = new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(properties.getKeyId())
				.build();

		return new ImmutableJWKSet<>(new JWKSet(rsaKey));
	}

	@Bean
	JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	private static Resource resolveExisting(ResourceLoader resourceLoader, String configuredLocation, String fileName) {
		for (String location : candidateLocations(configuredLocation, fileName)) {
			Resource resource = resourceLoader.getResource(location);
			if (resource.exists()) {
				return resource;
			}
		}
		return null;
	}

	private static List<String> candidateLocations(String configuredLocation, String fileName) {
		Set<String> locations = new LinkedHashSet<>();
		locations.add(configuredLocation);
		if (configuredLocation.startsWith("file:") && !isAbsoluteFileLocation(configuredLocation)) {
			locations.add("file:auth-server/keys/" + fileName);
			locations.add("file:./auth-server/keys/" + fileName);
			locations.add("file:keys/" + fileName);
			locations.add("file:./keys/" + fileName);
		}
		return new ArrayList<>(locations);
	}

	private static boolean isAbsoluteFileLocation(String location) {
		String path = location.substring("file:".length());
		return path.startsWith("/") || path.matches("^[A-Za-z]:[\\\\/].*");
	}

	private static Path resolveWritableKeyDirectory(RsaKeyProperties properties) {
		List<Path> directories = new ArrayList<>();
		addFileParentDirectory(directories, properties.getPrivateKeyLocation());
		addFileParentDirectory(directories, properties.getPublicKeyLocation());
		directories.add(Path.of("auth-server", "keys"));
		directories.add(Path.of("keys"));

		for (Path directory : directories) {
			try {
				Files.createDirectories(directory);
				Path probe = directory.resolve(".write-probe");
				Files.writeString(probe, "ok", StandardCharsets.UTF_8);
				Files.deleteIfExists(probe);
				return directory.toAbsolutePath().normalize();
			}
			catch (IOException ignored) {
			}
		}

		throw new IllegalStateException(
				"RSA 키 파일을 찾지 못했고 생성할 디렉터리도 쓸 수 없습니다. AUTH_RSA_PRIVATE_KEY / AUTH_RSA_PUBLIC_KEY를 확인하세요.");
	}

	private static void addFileParentDirectory(List<Path> directories, String location) {
		if (!location.startsWith("file:")) {
			return;
		}
		String path = location.substring("file:".length());
		if (path.startsWith("./")) {
			path = path.substring(2);
		}
		Path parent = Path.of(path).getParent();
		if (parent != null) {
			directories.add(parent);
		}
	}

	private static void generateAndStore(Path keyDirectory) {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			KeyPair keyPair = generator.generateKeyPair();

			Files.createDirectories(keyDirectory);
			Files.writeString(
					keyDirectory.resolve("private.pem"),
					toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded()),
					StandardCharsets.UTF_8);
			Files.writeString(
					keyDirectory.resolve("public.pem"),
					toPem("PUBLIC KEY", keyPair.getPublic().getEncoded()),
					StandardCharsets.UTF_8);
		}
		catch (Exception ex) {
			throw new IllegalStateException("RSA 키 쌍을 생성하지 못했습니다: " + keyDirectory, ex);
		}
	}

	private static String toPem(String type, byte[] encoded) {
		String body = Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(encoded);
		return "-----BEGIN " + type + "-----\n" + body + "\n-----END " + type + "-----\n";
	}

	private static RSAPublicKey readPublicKey(Resource resource) {
		try {
			byte[] decoded = decodePem(readResource(resource), "PUBLIC KEY");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
			return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
		}
		catch (Exception ex) {
			throw new IllegalStateException("RSA 공개키를 불러오지 못했습니다: " + describe(resource), ex);
		}
	}

	private static RSAPrivateKey readPrivateKey(Resource resource) {
		try {
			byte[] decoded = decodePem(readResource(resource), "PRIVATE KEY");
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
			return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
		}
		catch (Exception ex) {
			throw new IllegalStateException("RSA 개인키를 불러오지 못했습니다: " + describe(resource), ex);
		}
	}

	private static String readResource(Resource resource) throws IOException {
		if (!resource.exists()) {
			throw new IOException("리소스가 존재하지 않습니다: " + describe(resource));
		}
		try (InputStream inputStream = resource.getInputStream()) {
			return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static byte[] decodePem(String pem, String type) {
		String sanitized = pem
				.replace("-----BEGIN " + type + "-----", "")
				.replace("-----END " + type + "-----", "")
				.replaceAll("\\s", "");
		return Base64.getDecoder().decode(sanitized);
	}

	private static String describe(Resource resource) {
		try {
			return resource.getURI().toString();
		}
		catch (IOException ex) {
			return resource.getDescription();
		}
	}
}
