package com.sleekydz86.oidstudy.oidc.domain.repository;

import com.sleekydz86.oidstudy.oidc.domain.AccountStatus;
import com.sleekydz86.oidstudy.oidc.domain.UserAccount;
import com.sleekydz86.oidstudy.oidc.domain.UserIdentity;
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