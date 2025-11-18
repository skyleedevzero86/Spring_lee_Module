package com.sleekydz86.payment2v2.domain.member.model;

import com.sleekydz86.payment2v2.domain.member.model.valueobject.Email;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberName;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.Password;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members", indexes = {
    @Index(name = "idx_member_email", columnList = "email", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Column(nullable = false)
    private String password;

    @Embedded
    private MemberName name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MemberRole role;

    private Member(Email email, Password password, MemberName name, MemberRole role) {
        this.email = email;
        this.password = password.getEncodedValue();
        this.name = name;
        this.role = role;
    }

    public static Member create(Email email, Password password, MemberName name) {
        return new Member(email, password, name, MemberRole.USER);
    }

    public static Member createAdmin(Email email, Password password, MemberName name) {
        return new Member(email, password, name, MemberRole.ADMIN);
    }

    public boolean matchesPassword(String rawPassword, com.sleekydz86.payment2v2.domain.member.port.out.PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.password);
    }

    public void updatePassword(Password newPassword) {
        this.password = newPassword.getEncodedValue();
    }

    public boolean isAdmin() {
        return this.role == MemberRole.ADMIN;
    }

    public boolean isUser() {
        return this.role == MemberRole.USER;
    }

    public String getEmailValue() {
        return email != null ? email.getValue() : null;
    }

    public String getNameValue() {
        return name != null ? name.getValue() : null;
    }
}
