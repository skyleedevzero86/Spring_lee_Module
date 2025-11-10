package com.sleekydz86.passykey.domain.port.inbound;

import com.sleekydz86.passykey.application.dto.RegisterRequest;
import com.sleekydz86.passykey.domain.model.User;

public interface UserUseCase {
    User register(RegisterRequest request);
    User findByUsername(String username);
    User findByUserHandle(String userHandle);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}


