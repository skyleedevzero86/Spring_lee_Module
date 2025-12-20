package com.sleekydz86.passykey.domain.port.inbound;

import com.sleekydz86.passykey.application.dto.RegisterRequest;
import com.sleekydz86.passykey.domain.model.User;

public interface UserUseCase {
    User register(RegisterRequest request);
    User findByUsername(String username);
    User findByEmail(String email);
    User findByUsernameOrEmail(String identifier);
    User findByUserHandle(String userHandle);
    User findByDisplayName(String displayName);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}





