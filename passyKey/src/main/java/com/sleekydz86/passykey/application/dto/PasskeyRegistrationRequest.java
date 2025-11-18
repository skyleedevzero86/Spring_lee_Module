package com.sleekydz86.passykey.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PasskeyRegistrationRequest {

    @NotNull(message = "Public key is required")
    @Valid
    @JsonProperty("publicKey")
    private PublicKeyData publicKey;

    public PasskeyRegistrationRequest() {
    }

    public PublicKeyData getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKeyData publicKey) {
        this.publicKey = publicKey;
    }

    public static class PublicKeyData {
        @NotNull(message = "Credential is required")
        @Valid
        private CredentialData credential;

        @NotBlank(message = "Label is required")
        private String label;

        public PublicKeyData() {
        }

        public CredentialData getCredential() {
            return credential;
        }

        public void setCredential(CredentialData credential) {
            this.credential = credential;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    public static class CredentialData {
        @NotBlank(message = "ID is required")
        private String id;

        @NotBlank(message = "Raw ID is required")
        private String rawId;

        @NotNull(message = "Response is required")
        @Valid
        private ResponseData response;

        @NotBlank(message = "Type is required")
        private String type;

        public CredentialData() {
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

        public ResponseData getResponse() {
            return response;
        }

        public void setResponse(ResponseData response) {
            this.response = response;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class ResponseData {
        @NotBlank(message = "Attestation object is required")
        private String attestationObject;

        @NotBlank(message = "Client data JSON is required")
        private String clientDataJSON;

        private String[] transports;

        public ResponseData() {
        }

        public String getAttestationObject() {
            return attestationObject;
        }

        public void setAttestationObject(String attestationObject) {
            this.attestationObject = attestationObject;
        }

        public String getClientDataJSON() {
            return clientDataJSON;
        }

        public void setClientDataJSON(String clientDataJSON) {
            this.clientDataJSON = clientDataJSON;
        }

        public String[] getTransports() {
            return transports;
        }

        public void setTransports(String[] transports) {
            this.transports = transports;
        }
    }
}





