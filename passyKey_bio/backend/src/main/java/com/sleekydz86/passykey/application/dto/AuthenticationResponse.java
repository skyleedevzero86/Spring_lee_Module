package com.sleekydz86.passykey.application.dto;

public class AuthenticationResponse {
    private String redirectUrl;
    private boolean authenticated;
    private boolean passkeyLogin;

    public AuthenticationResponse() {
    }

    public AuthenticationResponse(String redirectUrl, boolean authenticated) {
        this.redirectUrl = redirectUrl;
        this.authenticated = authenticated;
        this.passkeyLogin = false;
    }

    public AuthenticationResponse(String redirectUrl, boolean authenticated, boolean passkeyLogin) {
        this.redirectUrl = redirectUrl;
        this.authenticated = authenticated;
        this.passkeyLogin = passkeyLogin;
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

    public boolean isPasskeyLogin() {
        return passkeyLogin;
    }

    public void setPasskeyLogin(boolean passkeyLogin) {
        this.passkeyLogin = passkeyLogin;
    }

    public static class Builder {
        private String redirectUrl;
        private boolean authenticated;
        private boolean passkeyLogin;

        public Builder redirectUrl(String redirectUrl) {
            this.redirectUrl = redirectUrl;
            return this;
        }

        public Builder authenticated(boolean authenticated) {
            this.authenticated = authenticated;
            return this;
        }

        public Builder passkeyLogin(boolean passkeyLogin) {
            this.passkeyLogin = passkeyLogin;
            return this;
        }

        public AuthenticationResponse build() {
            return new AuthenticationResponse(redirectUrl, authenticated, passkeyLogin);
        }
    }
}





