package com.sleekydz86.loginstudy.member.service;

import com.sleekydz86.loginstudy.member.api.MemberDtos.AddressRequest;
import com.sleekydz86.loginstudy.member.api.MemberDtos.AddressResponse;
import com.sleekydz86.loginstudy.member.api.MemberDtos.ChangeStatusRequest;
import com.sleekydz86.loginstudy.member.api.MemberDtos.KeysetPageResponse;
import com.sleekydz86.loginstudy.member.api.MemberDtos.MemberResponse;
import com.sleekydz86.loginstudy.member.api.MemberDtos.MemberSummaryResponse;
import com.sleekydz86.loginstudy.member.api.MemberDtos.PageResponse;
import com.sleekydz86.loginstudy.member.api.MemberDtos.PreferencesRequest;
import com.sleekydz86.loginstudy.member.api.MemberDtos.PreferencesResponse;
import com.sleekydz86.loginstudy.member.api.MemberDtos.SensitiveField;
import com.sleekydz86.loginstudy.member.api.MemberDtos.SensitiveFieldResponse;
import com.sleekydz86.loginstudy.member.api.MemberDtos.UpdateMemberRequest;
import com.sleekydz86.loginstudy.member.domain.MemberAddress;
import com.sleekydz86.loginstudy.member.domain.MemberPreferences;
import com.sleekydz86.loginstudy.member.domain.MemberProfile;
import com.sleekydz86.loginstudy.member.domain.MemberStatus;
import com.sleekydz86.loginstudy.member.repository.MemberProfileRepository;
import com.sleekydz86.loginstudy.member.repository.MemberProfileSpecs;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

	private static final Logger log = LoggerFactory.getLogger(MemberService.class);

	private final MemberProfileRepository memberProfileRepository;

	public MemberService(MemberProfileRepository memberProfileRepository) {
		this.memberProfileRepository = memberProfileRepository;
	}

	@Transactional(readOnly = true)
	public MemberResponse getBySubject(String subject) {
		MemberProfile profile = memberProfileRepository.findOneByUserSubject(subject)
				.orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다. subject=" + subject));
		return toResponse(profile);
	}

	@Transactional(readOnly = true)
	public MemberResponse getByIdForCaller(Long id, String callerSubject, boolean admin) {
		MemberProfile profile = memberProfileRepository.findOneById(id)
				.orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다. id=" + id));
		if (!admin && !profile.getUserSubject().equals(callerSubject)) {
			throw new AccessDeniedBusinessException("다른 회원의 프로필에 접근할 수 없습니다");
		}
		return toResponse(profile);
	}

	@Transactional
	public MemberResponse update(Long id, String callerSubject, boolean admin, UpdateMemberRequest request) {
		MemberProfile profile = memberProfileRepository.findOneById(id)
				.orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다. id=" + id));
		if (!admin && !profile.getUserSubject().equals(callerSubject)) {
			throw new AccessDeniedBusinessException("다른 회원의 프로필을 수정할 수 없습니다");
		}
		if (!profile.getVersion().equals(request.version())) {
			throw new OptimisticLockConflictException(
					"회원 버전 불일치. expected=" + profile.getVersion() + ", actual=" + request.version());
		}

		profile.changeDisplayName(request.displayName());
		applyAddress(profile, request.address());
		applyPreferences(profile, request.preferences());

		try {
			return toResponse(memberProfileRepository.saveAndFlush(profile));
		}
		catch (ObjectOptimisticLockingFailureException ex) {
			throw new OptimisticLockConflictException("동시 수정이 감지되었습니다. id=" + id);
		}
	}

	@Transactional
	public MemberResponse changeStatus(Long id, String adminSubject, ChangeStatusRequest request) {
		MemberProfile profile = memberProfileRepository.findOneById(id)
				.orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다. id=" + id));
		profile.changeStatus(request.status(), adminSubject, request.reason());
		return toResponse(memberProfileRepository.saveAndFlush(profile));
	}

	@Transactional(readOnly = true)
	public PageResponse<MemberSummaryResponse> search(
			MemberStatus status,
			String email,
			String name,
			Instant joinedFrom,
			Instant joinedTo,
			Pageable pageable) {
		String emailFilter = emptyToNull(email);
		String nameFilter = emptyToNull(name);
		Page<MemberProfile> page;
		if (emailFilter == null && nameFilter == null) {
			page = memberProfileRepository.findAll(
					MemberProfileSpecs.search(status, null, null, joinedFrom, joinedTo),
					pageable);
		}
		else {
			List<MemberProfile> filtered = memberProfileRepository.findAll(
							MemberProfileSpecs.search(status, null, null, joinedFrom, joinedTo),
							pageable.getSort())
					.stream()
					.filter(profile -> matchesSensitiveFilters(profile, emailFilter, nameFilter))
					.toList();
			int fromIndex = Math.min((int) pageable.getOffset(), filtered.size());
			int toIndex = Math.min(fromIndex + pageable.getPageSize(), filtered.size());
			page = new PageImpl<>(filtered.subList(fromIndex, toIndex), pageable, filtered.size());
		}
		return new PageResponse<>(
				page.getContent().stream().map(this::toSummary).toList(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}

	@Transactional(readOnly = true)
	public SensitiveFieldResponse revealSensitiveField(Long memberId, SensitiveField field, String adminSubject) {
		MemberProfile profile = memberProfileRepository.findOneById(memberId)
				.orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다. id=" + memberId));
		String value = switch (field) {
			case EMAIL -> profile.getEmail();
			case DISPLAY_NAME -> profile.getDisplayName();
		};
		log.info("관리자 민감정보 열람: adminSubject={}, memberId={}, field={}",
				adminSubject, memberId, field);
		return new SensitiveFieldResponse(memberId, field, value);
	}

	@Transactional(readOnly = true)
	public KeysetPageResponse searchKeyset(
			MemberStatus status,
			Instant joinedFrom,
			Instant joinedTo,
			Instant cursorJoinedAt,
			Long cursorId,
			int size) {
		int pageSize = Math.min(Math.max(size, 1), 100);
		List<MemberProfile> rows = memberProfileRepository.findAll(
				MemberProfileSpecs.keyset(status, joinedFrom, joinedTo, cursorJoinedAt, cursorId),
				PageRequest.of(0, pageSize + 1, Sort.by(
						Sort.Order.desc("joinedAt"),
						Sort.Order.desc("id")))).getContent();

		boolean hasNext = rows.size() > pageSize;
		List<MemberProfile> pageRows = hasNext ? rows.subList(0, pageSize) : rows;
		List<MemberSummaryResponse> content = pageRows.stream().map(this::toSummary).toList();
		MemberProfile last = pageRows.isEmpty() ? null : pageRows.get(pageRows.size() - 1);
		return new KeysetPageResponse(
				content,
				hasNext,
				last == null ? null : last.getId(),
				last == null ? null : last.getJoinedAt());
	}

	private void applyAddress(MemberProfile profile, AddressRequest addressRequest) {
		if (addressRequest == null) {
			return;
		}
		if (profile.getAddress() == null) {
			profile.attachAddress(new MemberAddress(
					addressRequest.countryCode(),
					addressRequest.city(),
					addressRequest.streetLine(),
					addressRequest.postalCode()));
		}
		else {
			profile.getAddress().update(
					addressRequest.countryCode(),
					addressRequest.city(),
					addressRequest.streetLine(),
					addressRequest.postalCode());
		}
	}

	private void applyPreferences(MemberProfile profile, PreferencesRequest preferencesRequest) {
		if (preferencesRequest == null) {
			return;
		}
		if (profile.getPreferences() == null) {
			profile.attachPreferences(new MemberPreferences(
					preferencesRequest.marketingOptIn(),
					preferencesRequest.locale(),
					preferencesRequest.timezone()));
		}
		else {
			profile.getPreferences().update(
					preferencesRequest.marketingOptIn(),
					preferencesRequest.locale(),
					preferencesRequest.timezone());
		}
	}

	private MemberResponse toResponse(MemberProfile profile) {
		AddressResponse address = null;
		if (profile.getAddress() != null) {
			address = new AddressResponse(
					profile.getAddress().getCountryCode(),
					profile.getAddress().getCity(),
					profile.getAddress().getStreetLine(),
					profile.getAddress().getPostalCode());
		}
		PreferencesResponse preferences = null;
		if (profile.getPreferences() != null) {
			preferences = new PreferencesResponse(
					profile.getPreferences().isMarketingOptIn(),
					profile.getPreferences().getLocale(),
					profile.getPreferences().getTimezone());
		}
		return new MemberResponse(
				profile.getId(),
				profile.getUserSubject(),
				profile.getEmail(),
				profile.getDisplayName(),
				profile.getStatus(),
				profile.getTenantId(),
				profile.getVersion(),
				profile.getJoinedAt(),
				profile.getUpdatedAt(),
				address,
				preferences);
	}

	private MemberSummaryResponse toSummary(MemberProfile profile) {
		return new MemberSummaryResponse(
				profile.getId(),
				maskEmail(profile.getEmail()),
				maskName(profile.getDisplayName()),
				profile.getStatus(),
				profile.getJoinedAt());
	}

	private static boolean matchesSensitiveFilters(
			MemberProfile profile,
			String emailFilter,
			String nameFilter) {
		boolean emailMatches = emailFilter == null
				|| profile.getEmail().equalsIgnoreCase(emailFilter);
		boolean nameMatches = nameFilter == null
				|| profile.getDisplayName().toLowerCase(Locale.ROOT)
						.contains(nameFilter.toLowerCase(Locale.ROOT));
		return emailMatches && nameMatches;
	}

	private static String maskName(String name) {
		if (name == null || name.isBlank()) {
			return "***";
		}
		int[] characters = name.codePoints().toArray();
		if (characters.length == 1) {
			return "*";
		}
		String first = new String(characters, 0, 1);
		if (characters.length == 2) {
			return first + "*";
		}
		String last = new String(characters, characters.length - 1, 1);
		return first + "*" + last;
	}

	private static String maskEmail(String email) {
		if (email == null || email.isBlank()) {
			return "***";
		}
		int at = email.indexOf('@');
		if (at <= 0) {
			return "***";
		}
		return email.substring(0, 1) + "***" + email.substring(at);
	}

	private static String emptyToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
