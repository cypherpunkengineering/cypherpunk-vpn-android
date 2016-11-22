package com.cypherpunk.privacy.data.api.json;


public class LoginResult {

    private String secret;

    private Privacy privacy;

    private Account account;

    public String getSecret() {
        return secret;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public Account getAccount() {
        return account;
    }

    public static class Privacy {
        public String username;
        public String password;
    }

    public static class Account {
        public String type;
        public boolean confirmed;
    }
}
