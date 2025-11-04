package com.sleekydz86.ocrstudy1.application.port.in;

import com.sleekydz86.ocrstudy1.doamin.model.IdVerification;

public interface IdVerificationUseCase {
    IdVerification verifyIdCard(Long imageId);
}