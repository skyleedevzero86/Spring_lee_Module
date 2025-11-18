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
@DisplayName("MemberRepository ?�합 ?�스??)
class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberJpaRepository memberRepository;

    @Test
    @DisplayName("?�원???�?�하�?조회?????�다")
    void ?�원???�?�하�?조회?????�다() {

        // given
        Member member = MemberFixture.?�반_?�용??);
        member = entityManager.persistAndFlush(member);


        Optional<Member> found = memberRepository.findById(member.getId());


        assertThat(found).isPresent();
        assertThat(found.get().getEmailValue()).isEqualTo("user@example.com");
        assertThat(found.get().getNameValue()).isEqualTo("?�길??);
    }

    @Test
    @DisplayName("?�메?�로 ?�원??조회?????�다")
    void ?�메?�로_?�원??조회?????�다() {


        Member member = MemberFixture.?�반_?�용??);

        entityManager.persistAndFlush(member);


        Optional<Member> found = memberRepository.findByEmail("user@example.com");



        assertThat(found).isPresent();
        assertThat(found.get().getEmailValue()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("?�메??중복 ?�인???�상?�으�??�작?�다")
    void ?�메??중복_?�인???�상?�으�??�작?�다() {


        Member member = MemberFixture.?�반_?�용??);

        entityManager.persistAndFlush(member);


        boolean exists = memberRepository.existsByEmail("user@example.com");
        boolean notExists = memberRepository.existsByEmail("new@example.com");



        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("?�름?�로 ?�원??검?�할 ???�다")
    void ?�름?�로_?�원??검?�할_???�다() {


        Member member1 = MemberFixture.?�반_?�용??);
        Member member2 = MemberFixture.?�메?�로_?�성("test2@example.com");

        entityManager.persistAndFlush(member1);
        entityManager.persistAndFlush(member2);


        List<Member> members = memberRepository.findByNameContainingIgnoreCase("?�길??);


        // then
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getNameValue()).isEqualTo("?�길??);
    }

    @Test
    @DisplayName("?�름?�로 ?�이�?검?�이 ?�상?�으�??�작?�다")
    void ?�름?�로_?�이�?검?�이_?�상?�으�??�작?�다() {

        for (int i = 1; i <= 25; i++) {

            Member member = MemberFixture.?�메?�로_?�성("user" + i + "@example.com");

            entityManager.persistAndFlush(member);

        }

        Pageable pageable = PageRequest.of(0, 10);


        Page<Member> memberPage = memberRepository.findByNameContainingIgnoreCase("?�스??, pageable);


        assertThat(memberPage.getContent()).hasSize(10);
        assertThat(memberPage.getTotalElements()).isEqualTo(25);
        assertThat(memberPage.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("?�메?�로 ?�원??검?�할 ???�다")
    void ?�메?�로_?�원??검?�할_???�다() {

        // given
        Member member1 = MemberFixture.?�반_?�용??);
        Member member2 = MemberFixture.?�메?�로_?�성("test2@example.com");
        // when
        entityManager.persistAndFlush(member1);
        entityManager.persistAndFlush(member2);


        List<Member> members = memberRepository.findByEmailContainingIgnoreCase("example");


        // then
        assertThat(members).hasSize(2);
    }

    @Test
    @DisplayName("?�?�문??구분 ?�이 ?�메?�로 조회?????�다")
    void ?�?�문??구분_?�이_?�메?�로_조회?????�다() {

        // given
        Member member = MemberFixture.?�반_?�용??);
        // when
        entityManager.persistAndFlush(member);


        Optional<Member> found = memberRepository.findByEmail("USER@EXAMPLE.COM");


        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmailValue()).isEqualTo("user@example.com");
    }




