package com.sleekydz86.payment2v2.domain.member.model;

import com.sleekydz86.payment2v2.domain.member.model.valueobject.Email;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberName;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.Password;
import com.sleekydz86.payment2v2.domain.member.port.out.PasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Member 도메인 모델 테스트")
class MemberTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("일반 사용자 회원 생성이 정상적으로 동작한다")
    void 일반_사용자_회원_생성이_정상적으로_동작한다() {

        Email email = Email.of("test@example.com");
        Password password = Password.ofEncoded("encoded-password");
        MemberName name = MemberName.of("홍길동");

        Member member = Member.create(email, password, name);

        assertThat(member.getEmailValue()).isEqualTo("test@example.com");
        assertThat(member.getNameValue()).isEqualTo("홍길동");
        assertThat(member.getRole()).isEqualTo(MemberRole.USER);
        assertThat(member.isUser()).isTrue();
        assertThat(member.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("관리자 회원 생성이 정상적으로 동작한다")
    void 관리자_회원_생성이_정상적으로_동작한다() {

        Email email = Email.of("admin@example.com");
        Password password = Password.ofEncoded("encoded-password");
        MemberName name = MemberName.of("관리자");

        Member member = Member.createAdmin(email, password, name);

        assertThat(member.getEmailValue()).isEqualTo("admin@example.com");
        assertThat(member.getNameValue()).isEqualTo("관리자");
        assertThat(member.getRole()).isEqualTo(MemberRole.ADMIN);
        assertThat(member.isAdmin()).isTrue();
        assertThat(member.isUser()).isFalse();
    }

    @Test
    @DisplayName("비밀번호 일치 인증이 정상적으로 동작한다")
    void 비밀번호_일치_인증이_정상적으로_동작한다() {

        Member member = Member.create(
                Email.of("test@example.com"),
                Password.ofEncoded("encoded-password"),
                MemberName.of("홍길동")
        );
        String rawPassword = "raw-password";

        given(passwordEncoder.matches(rawPassword, "encoded-password")).willReturn(true);

        boolean matches = member.matchesPassword(rawPassword, passwordEncoder);

        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("비밀번호 불일치 인증이 정상적으로 동작한다")
    void 비밀번호_불일치_인증이_정상적으로_동작한다() {

        Member member = Member.create(
                Email.of("test@example.com"),
                Password.ofEncoded("encoded-password"),
                MemberName.of("홍길동")
        );
        String rawPassword = "wrong-password";

        given(passwordEncoder.matches(rawPassword, "encoded-password")).willReturn(false);

        boolean matches = member.matchesPassword(rawPassword, passwordEncoder);

        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("비밀번호 업데이트가 정상적으로 동작한다")
    void 비밀번호_업데이트가_정상적으로_동작한다() {

        Member member = Member.create(
                Email.of("test@example.com"),
                Password.ofEncoded("old-encoded-password"),
                MemberName.of("홍길동")
        );
        Password newPassword = Password.ofEncoded("new-encoded-password");

        member.updatePassword(newPassword);

        given(passwordEncoder.matches("new-raw-password", "new-encoded-password")).willReturn(true);
        assertThat(member.matchesPassword("new-raw-password", passwordEncoder)).isTrue();
    }
}
