package com.sleekydz86.passykey.application.dto;

public class AuthenticationResponse {
    private String redirectUrl;
    private boolean authenticated;

    public AuthenticationResponse() {
    }

    public AuthenticationResponse(String redirectUrl, boolean authenticated) {
        this.redirectUrl = redirectUrl;
        this.authenticated = authenticated;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public static class Builder {
        private String redirectUrl;
        private boolean authenticated;

        public Builder redirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
            return this;
        }

        public Builder authenticated(boolean authenticated) {
            this.authenticated = authenticated;
            return this;
        }

        public AuthenticationResponse build() {
            return new AuthenticationResponse(redirectUrl, authenticated);
        }
    }
}


