package com.sleekydz86.passykey.global.config;

import com.sleekydz86.passykey.domain.port.outbound.WebAuthnConfigPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebAuthnConfigAdapter implements WebAuthnConfigPort {

    @Value("${webauthn.rp.id:localhost}")
    private String rpId;

    @Value("${webauthn.rp.name:PassyKey Application}")
    private String rpName;

    @Value("${webauthn.allowed.origins:http://localhost:8080}")
    private String allowedOrigins;

    @Override
    public String getRpId() {
        return rpId;
    }

    @Override
    public String getRpName() {
        return rpName;
    }

    @Override
    public String getAllowedOrigins() {
        return allowedOrigins;
    }
}

