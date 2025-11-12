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

    private User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public static User create(String email, String encodedPassword, String name) {
        return new User(email, encodedPassword, name);
    }

    public boolean matchesPassword(String rawPassword, com.sleekydz86.toaspayment.domain.user.PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.password);
    }
}

