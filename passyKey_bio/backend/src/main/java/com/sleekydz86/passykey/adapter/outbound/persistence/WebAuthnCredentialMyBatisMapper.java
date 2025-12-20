package com.sleekydz86.passykey.adapter.outbound.persistence;

import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface WebAuthnCredentialMyBatisMapper {

    void save(Map<String, Object> params);

    WebAuthnCredential selectById(Long id);

    WebAuthnCredential selectByCredentialId(String credentialId);

    List<WebAuthnCredential> selectByUserId(Long userId);

    void deleteByCredentialId(String credentialId);
}
