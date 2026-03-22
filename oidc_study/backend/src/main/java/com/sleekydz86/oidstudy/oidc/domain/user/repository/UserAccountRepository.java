package com.sleekydz86.oidstudy.oidc.domain.user.repository;

import com.sleekydz86.oidstudy.oidc.domain.user.AccountStatus;
import com.sleekydz86.oidstudy.oidc.domain.user.UserAccount;
import com.sleekydz86.oidstudy.oidc.domain.user.UserIdentity;
import java.util.List;
import java.util.Optional;

public interface UserAccountRepository {

    Optional<UserAccount> findByIdentity(UserIdentity identity);
    Optional<UserAccount> findById(Long id);
    Optional<UserAccount> findByLoginId(String loginId);
    List<UserAccount> findAll(AccountStatus status);
    long countAll();
    long countByStatus(AccountStatus status);
    UserAccount save(UserAccount account);
    UserAccount updateLogin(UserAccount account);
    UserAccount updateRegistration(UserAccount account);
    UserAccount updateStatus(UserAccount account);
}