package com.sleekydz86.passykey.domain.port.outbound;

public interface WebAuthnConfigPort {
    String getRpId();
    String getRpName();
    String getAllowedOrigins();
}

