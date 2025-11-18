package com.sleekydz86.payment2v2.domain.member.application.service;

import com.sleekydz86.payment2v2.domain.member.model.valueobject.Email;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberId;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberName;
import com.sleekydz86.payment2v2.domain.member.model.valueobject.Password;
import org.springframework.stereotype.Component;

@Component
public class MemberValidator {

    public Email validateEmail(String email) {
        return Email.of(email);
    }

    public void validatePassword(String rawPassword) {
        Password.validateRaw(rawPassword);
    }

    public MemberName validateName(String name) {
        return MemberName.of(name);
    }

    public MemberId validateMemberId(Long id) {
        return MemberId.of(id);
    }
}

