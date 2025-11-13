package com.sleekydz86.payment2v2.common.fixture;

import com.sleekydz86.payment2v2.domain.member.model.Member;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.Email;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberName;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.Password;

public class MemberFixture {

    public static Member 일반_사용자() {
        return Member.create(
                Email.of("user@example.com"),
                Password.ofEncoded("$2a$10$encodedPasswordHash"),
                MemberName.of("홍길동")
        );
    }

    public static Member 관리자() {
        return Member.createAdmin(
                Email.of("admin@example.com"),
                Password.ofEncoded("$2a$10$encodedPasswordHash"),
                MemberName.of("관리자")
        );
    }

    public static Member 이메일로_생성(String email) {
        return Member.create(
                Email.of(email),
                Password.ofEncoded("$2a$10$encodedPasswordHash"),
                MemberName.of("테스트 사용자")
        );
    }
}

