package com.sleekydz86.toaspayment.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private User(String email, String password, String name, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public static User create(String email, String encodedPassword, String name) {
        return new User(email, encodedPassword, name, UserRole.USER);
    }

    public static User createAdmin(String email, String encodedPassword, String name) {
        return new User(email, encodedPassword, name, UserRole.ADMIN);
    }

    public boolean matchesPassword(String rawPassword, com.sleekydz86.toaspayment.domain.user.PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.password);
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean isUser() {
        return this.role == UserRole.USER;
    }
}

