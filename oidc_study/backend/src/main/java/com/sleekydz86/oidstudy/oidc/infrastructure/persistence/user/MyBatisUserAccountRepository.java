package com.sleekydz86.oidstudy.oidc.infrastructure.persistence.user;

import java.util.List;
import java.util.Optional;
import com.sleekydz86.oidstudy.oidc.domain.AccountStatus;
import com.sleekydz86.oidstudy.oidc.domain.UserAccount;
import com.sleekydz86.oidstudy.oidc.domain.UserIdentity;
import com.sleekydz86.oidstudy.oidc.domain.repository.UserAccountRepository;
import com.sleekydz86.oidstudy.oidc.mapper.UserAccountMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisUserAccountRepository implements UserAccountRepository {

    private final UserAccountMapper userAccountMapper;

    public MyBatisUserAccountRepository(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public Optional<UserAccount> findByIdentity(UserIdentity identity) {
        return Optional.ofNullable(
                userAccountMapper.findByProviderAndProviderUserId(identity.provider(), identity.providerUserId())
        );
    }

    @Override
    public Optional<UserAccount> findById(Long id) {
        return Optional.ofNullable(userAccountMapper.findById(id));
    }

    @Override
    public Optional<UserAccount> findByLoginId(String loginId) {
        return Optional.ofNullable(userAccountMapper.findByLoginId(loginId));
    }

    @Override
    public List<UserAccount> findAll(AccountStatus status) {
        return userAccountMapper.findAll(status);
    }

    @Override
    public long countAll() {
        return userAccountMapper.countAll();
    }

    @Override
    public long countByStatus(AccountStatus status) {
        return userAccountMapper.countByStatus(status);
    }

    @Override
    public UserAccount save(UserAccount account) {
        userAccountMapper.insertUser(account);
        return account;
    }

    @Override
    public UserAccount updateLogin(UserAccount account) {
        userAccountMapper.updateOnLogin(account);
        return account;
    }

    @Override
    public UserAccount updateRegistration(UserAccount account) {
        userAccountMapper.updateOnRegistration(account);
        return account;
    }

    @Override
    public UserAccount updateStatus(UserAccount account) {
        userAccountMapper.updateStatus(account);
        return account;
    }
}