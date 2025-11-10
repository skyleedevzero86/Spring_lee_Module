package com.sleekydz86.passykey.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PasskeyAuthenticationRequest {

    @NotBlank(message = "ID is required")
    private String id;

    @NotBlank(message = "Raw ID is required")
    private String rawId;

    @NotNull(message = "Response is required")
    @Valid
    @JsonProperty("response")
    private AuthenticationResponseData response;

    public PasskeyAuthenticationRequest() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRawId() {
        return rawId;
    }

    public void setRawId(String rawId) {
        this.rawId = rawId;
    }

    public AuthenticationResponseData getResponse() {
        return response;
    }

    public void setResponse(AuthenticationResponseData response) {
        this.response = response;
    }

    public static class AuthenticationResponseData {
        @NotBlank(message = "Authenticator data is required")
        private String authenticatorData;

        @NotBlank(message = "Client data JSON is required")
        private String clientDataJSON;

        @NotBlank(message = "Signature is required")
        private String signature;

        private String userHandle;

        public AuthenticationResponseData() {
        }

        public String getAuthenticatorData() {
            return authenticatorData;
        }

        public void setAuthenticatorData(String authenticatorData) {
            this.authenticatorData = authenticatorData;
        }

        public String getClientDataJSON() {
            return clientDataJSON;
        }

        public void setClientDataJSON(String clientDataJSON) {
            this.clientDataJSON = clientDataJSON;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getUserHandle() {
            return userHandle;
        }

        public void setUserHandle(String userHandle) {
            this.userHandle = userHandle;
        }
    }
}



