package com.sleekydz86.payment2v2.domain.member.application.service;

import com.sleekydz86.payment2v2.common.fixture.MemberFixture;
import com.sleekydz86.payment2v2.domain.member.application.dto.*;
import com.sleekydz86.payment2v2.domain.member.model.Member;
import com.sleekydz86.payment2v2.domain.member.port.out.MemberRepository;
import com.sleekydz86.payment2v2.domain.member.port.out.PasswordEncoder;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.sleekydz86.payment2v2.global.dto.PageResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 단위 테스트")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원가입이 성공적으로 완료된다")
    void 회원가입이_성공적으로_완료된다() {
        // given
        RegisterMemberCommand command = RegisterMemberCommand.builder()
                .email("test@example.com")
                .name("홍길동")
                .password("password123")
                .build();

        Member savedMember = MemberFixture.일반_사용자();
        ReflectionTestUtils.setField(savedMember, "id", 1L);

        RegisterMemberResponse expectedResponse = RegisterMemberResponse.builder()
                .id(1L)
                .email("test@example.com")
                .name("홍길동")
                .build();

        given(memberRepository.existsByEmail("test@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("$2a$10$encodedPasswordHash");
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);
        given(memberMapper.toRegisterResponse(savedMember)).willReturn(expectedResponse);

        // when
        RegisterMemberResponse result = memberService.register(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("홍길동");

        verify(memberRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시 예외가 발생한다")
    void 이미_존재하는_이메일로_회원가입_시_예외가_발생한다() {
        // given
        RegisterMemberCommand command = RegisterMemberCommand.builder()
                .email("existing@example.com")
                .name("홍길동")
                .password("password123")
                .build();

        given(memberRepository.existsByEmail("existing@example.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.register(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEMBER_ALREADY_EXISTS);

        verify(memberRepository, never()).save(any(Member.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("이메일로 회원 조회가 성공적으로 완료된다")
    void 이메일로_회원_조회가_성공적으로_완료된다() {
        // given
        String email = "test@example.com";
        Member member = MemberFixture.일반_사용자();
        ReflectionTestUtils.setField(member, "id", 1L);

        FindMemberResponse expectedResponse = FindMemberResponse.builder()
                .id(1L)
                .email("test@example.com")
                .name("홍길동")
                .role("USER")
                .build();

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
        given(memberMapper.toFindResponse(member)).willReturn(expectedResponse);

        // when
        FindMemberResponse result = memberService.findByEmail(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("홍길동");

        verify(memberRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 예외가 발생한다")
    void 존재하지_않는_이메일로_조회_시_예외가_발생한다() {
        // given
        String email = "notfound@example.com";
        given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.findByEmail(email))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

        verify(memberRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("이름으로 회원 검색이 성공적으로 완료된다")
    void 이름으로_회원_검색이_성공적으로_완료된다() {
        // given
        String name = "홍길동";
        Member member1 = MemberFixture.일반_사용자();
        Member member2 = MemberFixture.이메일로_생성("test2@example.com");
        List<Member> members = List.of(member1, member2);

        SearchMemberResponse response1 = SearchMemberResponse.builder()
                .id(1L)
                .email("user@example.com")
                .name("홍길동")
                .build();
        SearchMemberResponse response2 = SearchMemberResponse.builder()
                .id(2L)
                .email("test2@example.com")
                .name("테스트 사용자")
                .build();

        given(memberRepository.findByNameContainingIgnoreCase("홍길동")).willReturn(members);
        given(memberMapper.toSearchResponse(member1)).willReturn(response1);
        given(memberMapper.toSearchResponse(member2)).willReturn(response2);

        // when
        List<SearchMemberResponse> result = memberService.searchByName(name);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("홍길동");

        verify(memberRepository, times(1)).findByNameContainingIgnoreCase("홍길동");
    }

    @Test
    @DisplayName("이름으로 페이징 검색이 성공적으로 완료된다")
    void 이름으로_페이징_검색이_성공적으로_완료된다() {
        // given
        String name = "홍길동";
        Pageable pageable = PageRequest.of(0, 10);
        Member member = MemberFixture.일반_사용자();
        Page<Member> memberPage = new PageImpl<>(List.of(member), pageable, 1);

        SearchMemberResponse response = SearchMemberResponse.builder()
                .id(1L)
                .email("user@example.com")
                .name("홍길동")
                .build();

        given(memberRepository.findByNameContainingIgnoreCase("홍길동", pageable)).willReturn(memberPage);
        given(memberMapper.toSearchResponse(member)).willReturn(response);

        // when
        PageResponse<SearchMemberResponse> result = memberService.searchByName(name, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);

        verify(memberRepository, times(1)).findByNameContainingIgnoreCase("홍길동", pageable);
    }

    @Test
    @DisplayName("비밀번호 재설정이 성공적으로 완료된다")
    void 비밀번호_재설정이_성공적으로_완료된다() {
        // given
        ResetPasswordCommand command = ResetPasswordCommand.builder()
                .email("test@example.com")
                .newPassword("newPassword123")
                .build();

        Member member = MemberFixture.일반_사용자();
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.encode("newPassword123")).willReturn("$2a$10$newEncodedPasswordHash");
        given(memberRepository.save(any(Member.class))).willReturn(member);

        // when
        ResetPasswordResponse result = memberService.resetPassword(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getMessage()).contains("성공적으로 재설정");

        verify(memberRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 비밀번호 재설정 시 예외가 발생한다")
    void 존재하지_않는_이메일로_비밀번호_재설정_시_예외가_발생한다() {
        // given
        ResetPasswordCommand command = ResetPasswordCommand.builder()
                .email("notfound@example.com")
                .newPassword("newPassword123")
                .build();

        given(memberRepository.findByEmail("notfound@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.resetPassword(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEMBER_EMAIL_NOT_FOUND);

        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }
}

