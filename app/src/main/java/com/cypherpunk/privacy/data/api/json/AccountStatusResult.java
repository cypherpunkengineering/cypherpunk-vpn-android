package com.cypherpunk.privacy.data.api.json;


public class AccountStatusResult {

    private String secret;

    private Privacy privacy;

    private Account account;

    private Subscription subscription;

    public String getSecret() {
        return secret;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public Account getAccount() {
        return account;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public static class Privacy {
        public String username;
        public String password;
    }

    public static class Account {
        public String id;
        public String email;
        // "premium" or "free"
        public String type;
        public boolean confirmed;
    }

    public static class Subscription {
        // "none", "monthly", "semiannually", "annually", "forever", "lifetime"
        public String renewal;
        public String expiration;
    }
}
