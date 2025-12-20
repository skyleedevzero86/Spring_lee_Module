package com.sleekydz86.passykey.domain.port.outbound;

import com.sleekydz86.passykey.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserHandle(String userHandle);
    Optional<User> findByDisplayName(String displayName);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}





