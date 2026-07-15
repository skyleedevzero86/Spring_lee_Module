package com.sleekydz86.loginstudy.auth.config;

import com.sleekydz86.loginstudy.auth.domain.UserAccount;
import com.sleekydz86.loginstudy.auth.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthDataInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AuthDataInitializer.class);

	public static final String DEMO_USER_PASSWORD = "ChangeMe123!";
	public static final String USER_PORTAL_SECRET = "user-portal-secret";
	public static final String ADMIN_PORTAL_SECRET = "admin-portal-secret";
	public static final String MEMBER_SERVICE_SECRET = "member-service-secret";

	private final RegisteredClientRepository registeredClientRepository;
	private final UserAccountRepository userAccountRepository;
	private final PasswordEncoder passwordEncoder;
	private final String tenantId;

	public AuthDataInitializer(
			RegisteredClientRepository registeredClientRepository,
			UserAccountRepository userAccountRepository,
			PasswordEncoder passwordEncoder,
			@Value("${auth.bootstrap.tenant-id:tenant-demo}") String tenantId) {
		this.registeredClientRepository = registeredClientRepository;
		this.userAccountRepository = userAccountRepository;
		this.passwordEncoder = passwordEncoder;
		this.tenantId = tenantId;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		seedClients();
		seedUsers();
	}

	private void seedClients() {
		if (registeredClientRepository.findByClientId(AuthorizationServerConfig.CLIENT_USER_PORTAL) == null) {
			registeredClientRepository.save(
					AuthorizationServerConfig.userPortalClient(passwordEncoder, USER_PORTAL_SECRET));
			log.info("Registered OAuth2 client: {}", AuthorizationServerConfig.CLIENT_USER_PORTAL);
		}
		if (registeredClientRepository.findByClientId(AuthorizationServerConfig.CLIENT_ADMIN_PORTAL) == null) {
			registeredClientRepository.save(
					AuthorizationServerConfig.adminPortalClient(passwordEncoder, ADMIN_PORTAL_SECRET));
			log.info("Registered OAuth2 client: {}", AuthorizationServerConfig.CLIENT_ADMIN_PORTAL);
		}
		if (registeredClientRepository.findByClientId(AuthorizationServerConfig.CLIENT_MEMBER_SERVICE) == null) {
			registeredClientRepository.save(
					AuthorizationServerConfig.memberServiceClient(passwordEncoder, MEMBER_SERVICE_SECRET));
			log.info("Registered OAuth2 client: {}", AuthorizationServerConfig.CLIENT_MEMBER_SERVICE);
		}
	}

	private void seedUsers() {
		if (!userAccountRepository.existsByUsername("user")) {
			UserAccount user = new UserAccount(
					"user",
					passwordEncoder.encode(DEMO_USER_PASSWORD),
					"user@loginstudy.local",
					tenantId);
			user.addRole("USER");
			userAccountRepository.save(user);
			log.info("Seeded user account: user");
		}
		if (!userAccountRepository.existsByUsername("admin")) {
			UserAccount admin = new UserAccount(
					"admin",
					passwordEncoder.encode(DEMO_USER_PASSWORD),
					"admin@loginstudy.local",
					tenantId);
			admin.addRole("USER");
			admin.addRole("ADMIN");
			userAccountRepository.save(admin);
			log.info("Seeded user account: admin");
		}
	}
}
