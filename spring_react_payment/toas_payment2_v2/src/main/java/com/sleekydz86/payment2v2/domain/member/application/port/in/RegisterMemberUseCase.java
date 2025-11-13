package com.sleekydz86.payment2v2.domain.member.application.port.in;

import com.sleekydz86.payment2v2.domain.member.application.dto.RegisterMemberCommand;
import com.sleekydz86.payment2v2.domain.member.application.dto.RegisterMemberResponse;

public interface RegisterMemberUseCase {
    RegisterMemberResponse register(RegisterMemberCommand command);
}

