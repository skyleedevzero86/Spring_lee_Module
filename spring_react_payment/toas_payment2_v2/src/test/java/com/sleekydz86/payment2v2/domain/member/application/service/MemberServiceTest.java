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
@DisplayName("MemberService ?�위 ?�스??)
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
    @DisplayName("?�원가?�이 ?�공?�으�??�료?�다")
    void ?�원가?�이_?�공?�으�??�료?�다() {

        // given
        RegisterMemberCommand command = RegisterMemberCommand.builder()
                .email("test@example.com")
                .name("?�길??)
                .password("password123")
                .build();

        Member savedMember = MemberFixture.?�반_?�용??);
        // when
        ReflectionTestUtils.setField(savedMember, "id", 1L);

        RegisterMemberResponse expectedResponse = RegisterMemberResponse.builder()
                .id(1L)
                .email("test@example.com")
                .name("?�길??)
                .build();

        given(memberRepository.existsByEmail("test@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("$2a$10$encodedPasswordHash");
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);
        given(memberMapper.toRegisterResponse(savedMember)).willReturn(expectedResponse);


        RegisterMemberResponse result = memberService.register(command);


        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("?�길??);

        verify(memberRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("?��? 존재?�는 ?�메?�로 ?�원가?????�외가 발생?�다")
    void ?��?_존재?�는_?�메?�로_?�원가?????�외가_발생?�다() {

        // given
        RegisterMemberCommand command = RegisterMemberCommand.builder()
                .email("existing@example.com")
                .name("?�길??)
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
    @DisplayName("?�메?�로 ?�원 조회가 ?�공?�으�??�료?�다")
    void ?�메?�로_?�원_조회가_?�공?�으�??�료?�다() {

        // given
        String email = "test@example.com";
        Member member = MemberFixture.?�반_?�용??);
        // when
        ReflectionTestUtils.setField(member, "id", 1L);

        FindMemberResponse expectedResponse = FindMemberResponse.builder()
                .id(1L)
                .email("test@example.com")
                .name("?�길??)
                .role("USER")
                .build();

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
        given(memberMapper.toFindResponse(member)).willReturn(expectedResponse);


        FindMemberResponse result = memberService.findByEmail(email);


        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getName()).isEqualTo("?�길??);

        verify(memberRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("존재?��? ?�는 ?�메?�로 조회 ???�외가 발생?�다")
    void 존재?��?_?�는_?�메?�로_조회_???�외가_발생?�다() {

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
    @DisplayName("?�름?�로 ?�원 검?�이 ?�공?�으�??�료?�다")
    void ?�름?�로_?�원_검?�이_?�공?�으�??�료?�다() {

        // given
        String name = "?�길??;
        Member member1 = MemberFixture.?�반_?�용??);
        Member member2 = MemberFixture.?�메?�로_?�성("test2@example.com");
        List<Member> members = List.of(member1, member2);

        SearchMemberResponse response1 = SearchMemberResponse.builder()
                .id(1L)
                .email("user@example.com")
                .name("?�길??)
                .build();
        SearchMemberResponse response2 = SearchMemberResponse.builder()
                .id(2L)
                .email("test2@example.com")
                .name("?�스???�용??)
                .build();

        given(memberRepository.findByNameContainingIgnoreCase("?�길??)).willReturn(members);
        given(memberMapper.toSearchResponse(member1)).willReturn(response1);
        given(memberMapper.toSearchResponse(member2)).willReturn(response2);


        List<SearchMemberResponse> result = memberService.searchByName(name);


        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("?�길??);

        verify(memberRepository, times(1)).findByNameContainingIgnoreCase("?�길??);
    }

    @Test
    @DisplayName("?�름?�로 ?�이�?검?�이 ?�공?�으�??�료?�다")
    void ?�름?�로_?�이�?검?�이_?�공?�으�??�료?�다() {

        // given
        String name = "?�길??;
        Pageable pageable = PageRequest.of(0, 10);
        Member member = MemberFixture.?�반_?�용??);
        Page<Member> memberPage = new PageImpl<>(List.of(member), pageable, 1);

        SearchMemberResponse response = SearchMemberResponse.builder()
                .id(1L)
                .email("user@example.com")
                .name("?�길??)
                .build();

        given(memberRepository.findByNameContainingIgnoreCase("?�길??, pageable)).willReturn(memberPage);
        given(memberMapper.toSearchResponse(member)).willReturn(response);


        PageResponse<SearchMemberResponse> result = memberService.searchByName(name, pageable);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);

        verify(memberRepository, times(1)).findByNameContainingIgnoreCase("?�길??, pageable);
    }

    @Test
    @DisplayName("비�?번호 ?�설?�이 ?�공?�으�??�료?�다")
    void 비�?번호_?�설?�이_?�공?�으�??�료?�다() {

        // given
        ResetPasswordCommand command = ResetPasswordCommand.builder()
                .email("test@example.com")
                .newPassword("newPassword123")
                .build();

        Member member = MemberFixture.?�반_?�용??);
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.encode("newPassword123")).willReturn("$2a$10$newEncodedPasswordHash");
        given(memberRepository.save(any(Member.class))).willReturn(member);


        ResetPasswordResponse result = memberService.resetPassword(command);


        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getMessage()).contains("?�공?�으�??�설??);

        verify(memberRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("존재?��? ?�는 ?�메?�로 비�?번호 ?�설?????�외가 발생?�다")
    void 존재?��?_?�는_?�메?�로_비�?번호_?�설?????�외가_발생?�다() {

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

