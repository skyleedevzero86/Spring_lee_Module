package com.sleekydz86.passykey.global.security;

import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.port.inbound.UserUseCase;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceAdapter implements UserDetailsService {

    private final UserUseCase userUseCase;

    public UserDetailsServiceAdapter(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userUseCase.findByUsername(username);
    }
}

