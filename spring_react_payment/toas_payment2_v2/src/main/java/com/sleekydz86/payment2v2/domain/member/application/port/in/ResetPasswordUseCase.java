package com.sleekydz86.payment2v2.domain.member.application.port.in;

import com.sleekydz86.payment2v2.domain.member.application.dto.ResetPasswordCommand;
import com.sleekydz86.payment2v2.domain.member.application.dto.ResetPasswordResponse;

public interface ResetPasswordUseCase {
    ResetPasswordResponse resetPassword(ResetPasswordCommand command);
}

