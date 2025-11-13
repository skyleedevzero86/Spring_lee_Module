package com.sleekydz86.payment2v2.domain.member.adapter.out.persistence;

import com.sleekydz86.payment2v2.common.fixture.MemberFixture;
import com.sleekydz86.payment2v2.domain.member.model.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("MemberRepository 통합 테스트")
class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberJpaRepository memberRepository;

    @Test
    @DisplayName("회원을 저장하고 조회할 수 있다")
    void 회원을_저장하고_조회할_수_있다() {
        // given
        Member member = MemberFixture.일반_사용자();
        member = entityManager.persistAndFlush(member);

        // when
        Optional<Member> found = memberRepository.findById(member.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmailValue()).isEqualTo("user@example.com");
        assertThat(found.get().getNameValue()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("이메일로 회원을 조회할 수 있다")
    void 이메일로_회원을_조회할_수_있다() {
        // given
        Member member = MemberFixture.일반_사용자();
        entityManager.persistAndFlush(member);

        // when
        Optional<Member> found = memberRepository.findByEmail("user@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmailValue()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("이메일 중복 확인이 정상적으로 동작한다")
    void 이메일_중복_확인이_정상적으로_동작한다() {
        // given
        Member member = MemberFixture.일반_사용자();
        entityManager.persistAndFlush(member);

        // when
        boolean exists = memberRepository.existsByEmail("user@example.com");
        boolean notExists = memberRepository.existsByEmail("new@example.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("이름으로 회원을 검색할 수 있다")
    void 이름으로_회원을_검색할_수_있다() {
        // given
        Member member1 = MemberFixture.일반_사용자();
        Member member2 = MemberFixture.이메일로_생성("test2@example.com");
        entityManager.persistAndFlush(member1);
        entityManager.persistAndFlush(member2);

        // when
        List<Member> members = memberRepository.findByNameContainingIgnoreCase("홍길동");

        // then
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getNameValue()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("이름으로 페이징 검색이 정상적으로 동작한다")
    void 이름으로_페이징_검색이_정상적으로_동작한다() {
        // given
        for (int i = 1; i <= 25; i++) {
            Member member = MemberFixture.이메일로_생성("user" + i + "@example.com");
            entityManager.persistAndFlush(member);
        }

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Member> memberPage = memberRepository.findByNameContainingIgnoreCase("테스트", pageable);

        // then
        assertThat(memberPage.getContent()).hasSize(10);
        assertThat(memberPage.getTotalElements()).isEqualTo(25);
        assertThat(memberPage.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("이메일로 회원을 검색할 수 있다")
    void 이메일로_회원을_검색할_수_있다() {
        // given
        Member member1 = MemberFixture.일반_사용자();
        Member member2 = MemberFixture.이메일로_생성("test2@example.com");
        entityManager.persistAndFlush(member1);
        entityManager.persistAndFlush(member2);

        // when
        List<Member> members = memberRepository.findByEmailContainingIgnoreCase("example");

        // then
        assertThat(members).hasSize(2);
    }

    @Test
    @DisplayName("대소문자 구분 없이 이메일로 조회할 수 있다")
    void 대소문자_구분_없이_이메일로_조회할_수_있다() {
        // given
        Member member = MemberFixture.일반_사용자();
        entityManager.persistAndFlush(member);

        // when
        Optional<Member> found = memberRepository.findByEmail("USER@EXAMPLE.COM");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmailValue()).isEqualTo("user@example.com");
    }
}

