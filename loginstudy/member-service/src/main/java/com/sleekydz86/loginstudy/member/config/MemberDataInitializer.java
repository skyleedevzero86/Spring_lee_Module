package com.sleekydz86.loginstudy.member.config;

import com.sleekydz86.loginstudy.member.domain.MemberAddress;
import com.sleekydz86.loginstudy.member.domain.MemberPreferences;
import com.sleekydz86.loginstudy.member.domain.MemberProfile;
import com.sleekydz86.loginstudy.member.domain.MemberStatus;
import com.sleekydz86.loginstudy.member.repository.MemberProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MemberDataInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(MemberDataInitializer.class);

	private final MemberProfileRepository memberProfileRepository;
	private final String defaultTenantId;

	public MemberDataInitializer(
			MemberProfileRepository memberProfileRepository,
			@Value("${member.api.default-tenant-id:tenant-demo}") String defaultTenantId) {
		this.memberProfileRepository = memberProfileRepository;
		this.defaultTenantId = defaultTenantId;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		seed("user", "user@loginstudy.local", "Demo User");
		seed("admin", "admin@loginstudy.local", "Demo Admin");
	}

	private void seed(String subject, String email, String displayName) {
		if (memberProfileRepository.existsByUserSubject(subject)) {
			return;
		}
		MemberProfile profile = new MemberProfile(subject, email, displayName, MemberStatus.ACTIVE, defaultTenantId);
		profile.attachAddress(new MemberAddress("KR", "Seoul", "Teheran-ro 1", "06236"));
		profile.attachPreferences(new MemberPreferences(false, "ko-KR", "Asia/Seoul"));
		memberProfileRepository.save(profile);
		log.info("회원 프로필을 시드했습니다. subject={}", subject);
	}
}
