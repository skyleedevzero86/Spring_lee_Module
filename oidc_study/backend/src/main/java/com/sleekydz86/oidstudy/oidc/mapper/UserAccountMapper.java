package com.sleekydz86.oidstudy.oidc.mapper;

import java.util.List;

import com.sleekydz86.oidstudy.oidc.domain.AccountStatus;
import com.sleekydz86.oidstudy.oidc.domain.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAccountMapper {

    UserAccount findByProviderAndProviderUserId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);
    UserAccount findById(@Param("id") Long id);
    UserAccount findByLoginId(@Param("loginId") String loginId);
    List<UserAccount> findAll(@Param("status") AccountStatus status);
    long countAll();
    long countByStatus(@Param("status") AccountStatus status);
    void insertUser(UserAccount userAccount);
    void updateOnLogin(UserAccount userAccount);
    void updateOnRegistration(UserAccount userAccount);
    void updateStatus(UserAccount userAccount);
}