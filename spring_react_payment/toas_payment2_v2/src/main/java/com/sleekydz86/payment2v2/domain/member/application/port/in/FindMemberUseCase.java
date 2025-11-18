package com.sleekydz86.payment2v2.domain.member.application.port.in;

import com.sleekydz86.payment2v2.domain.member.application.dto.FindMemberResponse;

public interface FindMemberUseCase {
    FindMemberResponse findByEmail(String email);
    FindMemberResponse findById(Long id);
}
