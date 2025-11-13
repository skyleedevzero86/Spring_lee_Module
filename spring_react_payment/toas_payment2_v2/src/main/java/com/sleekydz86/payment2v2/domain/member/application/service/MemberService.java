package com.sleekydz86.payment2v2.domain.member.application.service;

import com.sleekydz86.payment2v2.domain.member.application.dto.*;
import com.sleekydz86.payment2v2.domain.member.application.port.in.FindMemberUseCase;
import com.sleekydz86.payment2v2.domain.member.application.port.in.RegisterMemberUseCase;
import com.sleekydz86.payment2v2.domain.member.application.port.in.ResetPasswordUseCase;
import com.sleekydz86.payment2v2.domain.member.application.port.in.SearchMemberUseCase;
import com.sleekydz86.payment2v2.domain.member.application.port.in.SearchMemberPageUseCase;
import com.sleekydz86.payment2v2.domain.member.model.Member;
import com.sleekydz86.payment2v2.domain.member.model.PasswordEncoder;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.Email;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberId;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberName;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.Password;
import com.sleekydz86.payment2v2.domain.member.port.out.MemberRepository;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import com.sleekydz86.payment2v2.global.util.LoggingUtil;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService implements RegisterMemberUseCase, FindMemberUseCase, SearchMemberUseCase, ResetPasswordUseCase, SearchMemberPageUseCase {

    private static final String LOG_MEMBER_ID = "memberId";
    private static final String LOG_EMAIL = "email";
    private static final String LOG_OPERATION = "operation";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;

    @Override
    public RegisterMemberResponse register(RegisterMemberCommand command) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_EMAIL, command.getEmail() != null ? command.getEmail() : "알수없음",
                LOG_OPERATION, "register"
        ), () -> {
            log.info("회원가입 요청");

            Email email = Email.of(command.getEmail());
            Password.validateRaw(command.getPassword());
            MemberName name = MemberName.of(command.getName());

            if (memberRepository.existsByEmail(email.getValue())) {
                log.warn("이미 존재하는 이메일: {}", email.getValue());
                throw new BusinessException(ErrorCode.MEMBER_ALREADY_EXISTS);
            }

            String encodedPassword = passwordEncoder.encode(command.getPassword());
            Password password = Password.ofEncoded(encodedPassword);
            Member member = Member.create(email, password, name);
            member = memberRepository.save(member);

            log.info("회원가입 완료: id={}", member.getId());

            return memberMapper.toRegisterResponse(member);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public FindMemberResponse findByEmail(String emailValue) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_EMAIL, emailValue != null ? emailValue : "알수없음",
                LOG_OPERATION, "findByEmail"
        ), () -> {
            log.info("회원 검색 요청 (이메일)");

            Email email = Email.of(emailValue);
            Member member = memberRepository.findByEmail(email.getValue())
                    .orElseThrow(() -> {
                        log.warn("회원을 찾을 수 없음: email={}", email.getValue());
                        return new BusinessException(ErrorCode.MEMBER_NOT_FOUND,
                                String.format("이메일로 회원을 찾을 수 없습니다. email: %s", email.getValue()));
                    });

            return memberMapper.toFindResponse(member);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public FindMemberResponse findById(Long idValue) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_MEMBER_ID, idValue != null ? String.valueOf(idValue) : "알수없음",
                LOG_OPERATION, "findById"
        ), () -> {
            log.info("회원 검색 요청 (ID)");

            MemberId id = MemberId.of(idValue);
            Member member = memberRepository.findById(id.getValue())
                    .orElseThrow(() -> {
                        log.warn("회원을 찾을 수 없음: id={}", id.getValue());
                        return new BusinessException(ErrorCode.MEMBER_NOT_FOUND,
                                String.format("회원을 찾을 수 없습니다. id: %d", id.getValue()));
                    });

            return memberMapper.toFindResponse(member);
        });
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "memberSearch", key = "'name_' + #nameValue", unless = "#result.isEmpty()")
    public List<SearchMemberResponse> searchByName(String nameValue) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_OPERATION, "searchByName"
        ), () -> {
            log.info("회원 이름 검색 요청: name={}", nameValue);

            MemberName name = MemberName.of(nameValue);
            List<Member> members = memberRepository.findByNameContainingIgnoreCase(name.getValue());

            log.debug("검색 결과: {}건", members.size());
            return members.stream()
                    .map(memberMapper::toSearchResponse)
                    .toList();
        });
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "memberSearch", key = "'email_' + #emailValue", unless = "#result.isEmpty()")
    public List<SearchMemberResponse> searchByEmail(String emailValue) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_OPERATION, "searchByEmail"
        ), () -> {
            log.info("회원 이메일 검색 요청: email={}", emailValue);

            Email email = Email.of(emailValue);
            List<Member> members = memberRepository.findByEmailContainingIgnoreCase(email.getValue());

            log.debug("검색 결과: {}건", members.size());
            return members.stream()
                    .map(memberMapper::toSearchResponse)
                    .toList();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchMemberResponse> searchAll() {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_OPERATION, "searchAll"
        ), () -> {
            log.info("전체 회원 검색 요청");

            List<Member> members = memberRepository.findAll();

            log.debug("검색 결과: {}건", members.size());
            return members.stream()
                    .map(memberMapper::toSearchResponse)
                    .toList();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SearchMemberResponse> searchByName(String nameValue, Pageable pageable) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_OPERATION, "searchByNamePage"
        ), () -> {
            log.info("회원 이름 페이징 검색 요청: name={}, page={}, size={}", nameValue, pageable.getPageNumber(), pageable.getPageSize());

            MemberName name = MemberName.of(nameValue);
            Page<Member> memberPage = memberRepository.findByNameContainingIgnoreCase(name.getValue(), pageable);

            List<SearchMemberResponse> content = memberPage.getContent().stream()
                    .map(memberMapper::toSearchResponse)
                    .toList();

            return PageResponse.<SearchMemberResponse>builder()
                    .content(content)
                    .page(memberPage.getNumber())
                    .size(memberPage.getSize())
                    .totalElements(memberPage.getTotalElements())
                    .totalPages(memberPage.getTotalPages())
                    .hasNext(memberPage.hasNext())
                    .hasPrevious(memberPage.hasPrevious())
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SearchMemberResponse> searchByEmail(String emailValue, Pageable pageable) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_OPERATION, "searchByEmailPage"
        ), () -> {
            log.info("회원 이메일 페이징 검색 요청: email={}, page={}, size={}", emailValue, pageable.getPageNumber(), pageable.getPageSize());

            Email email = Email.of(emailValue);
            Page<Member> memberPage = memberRepository.findByEmailContainingIgnoreCase(email.getValue(), pageable);

            List<SearchMemberResponse> content = memberPage.getContent().stream()
                    .map(memberMapper::toSearchResponse)
                    .toList();

            return PageResponse.<SearchMemberResponse>builder()
                    .content(content)
                    .page(memberPage.getNumber())
                    .size(memberPage.getSize())
                    .totalElements(memberPage.getTotalElements())
                    .totalPages(memberPage.getTotalPages())
                    .hasNext(memberPage.hasNext())
                    .hasPrevious(memberPage.hasPrevious())
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SearchMemberResponse> searchAll(Pageable pageable) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_OPERATION, "searchAllPage"
        ), () -> {
            log.info("전체 회원 페이징 검색 요청: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

            Page<Member> memberPage = memberRepository.findAll(pageable);

            List<SearchMemberResponse> content = memberPage.getContent().stream()
                    .map(memberMapper::toSearchResponse)
                    .toList();

            return PageResponse.<SearchMemberResponse>builder()
                    .content(content)
                    .page(memberPage.getNumber())
                    .size(memberPage.getSize())
                    .totalElements(memberPage.getTotalElements())
                    .totalPages(memberPage.getTotalPages())
                    .hasNext(memberPage.hasNext())
                    .hasPrevious(memberPage.hasPrevious())
                    .build();
        });
    }

    @Override
    public ResetPasswordResponse resetPassword(ResetPasswordCommand command) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_EMAIL, command.getEmail() != null ? command.getEmail() : "알수없음",
                LOG_OPERATION, "resetPassword"
        ), () -> {
            log.info("비밀번호 재설정 요청");

            Email email = Email.of(command.getEmail());
            Password.validateRaw(command.getNewPassword());

            Member member = memberRepository.findByEmail(email.getValue())
                    .orElseThrow(() -> {
                        log.warn("회원을 찾을 수 없음: email={}", email.getValue());
                        return new BusinessException(ErrorCode.MEMBER_EMAIL_NOT_FOUND,
                                String.format("해당 이메일로 등록된 회원을 찾을 수 없습니다. email: %s", email.getValue()));
                    });

            String encodedPassword = passwordEncoder.encode(command.getNewPassword());
            Password newPassword = Password.ofEncoded(encodedPassword);
            member.updatePassword(newPassword);
            memberRepository.save(member);

            log.info("비밀번호 재설정 완료: email={}", email.getValue());

            return ResetPasswordResponse.builder()
                    .message("비밀번호가 성공적으로 재설정되었습니다.")
                    .email(email.getValue())
                    .build();
        });
    }
}
