package com.sleekydz86.payment2v2.domain.member.application.port.in;

import com.sleekydz86.payment2v2.domain.member.application.dto.LoginCommand;
import com.sleekydz86.payment2v2.domain.member.application.dto.LoginResponse;

public interface LoginUseCase {
    LoginResponse login(LoginCommand command);
}



