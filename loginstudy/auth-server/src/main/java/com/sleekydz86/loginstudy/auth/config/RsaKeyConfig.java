package com.sleekydz86.loginstudy.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;

@Configuration
@EnableConfigurationProperties(RsaKeyProperties.class)
public class RsaKeyConfig {

	@Bean
	JWKSource<SecurityContext> jwkSource(RsaKeyProperties properties, ResourceLoader resourceLoader) {
		RSAPublicKey publicKey = readPublicKey(resourceLoader.getResource(properties.getPublicKeyLocation()));
		RSAPrivateKey privateKey = readPrivateKey(resourceLoader.getResource(properties.getPrivateKeyLocation()));

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

	private static RSAPublicKey readPublicKey(Resource resource) {
		try {
			byte[] decoded = decodePem(readResource(resource), "PUBLIC KEY");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
			return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
		}
		catch (Exception ex) {
			throw new IllegalStateException(
					"RSA 공개키를 불러오지 못했습니다: " + describe(resource)
							+ ". 먼저 scripts/generate-rsa-keys.ps1을 실행하세요.",
					ex);
		}
	}

	private static RSAPrivateKey readPrivateKey(Resource resource) {
		try {
			byte[] decoded = decodePem(readResource(resource), "PRIVATE KEY");
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
			return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
		}
		catch (Exception ex) {
			throw new IllegalStateException(
					"RSA 개인키를 불러오지 못했습니다: " + describe(resource)
							+ ". 먼저 scripts/generate-rsa-keys.ps1을 실행하세요. "
							+ "개인키는 Git에 올리지 말고, 운영에서는 Vault/Secrets Manager 등으로 주입하세요.",
					ex);
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
