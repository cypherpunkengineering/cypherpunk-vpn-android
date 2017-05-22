package com.cypherpunk.privacy.domain.model;

import android.support.annotation.NonNull;

/**
 * kind of tunnel mode
 */
public enum TunnelMode {
    RECOMMENDED(Cipher.AES_128_GCM, Auth.SHA_256, Key.RSA_4096),
    MAX_SPEED(Cipher.NONE, Auth.SHA_256, Key.RSA_4096),
    MAX_PRIVACY(Cipher.AES_256_GCM, Auth.SHA_256, Key.RSA_4096),
    MAX_STEALTH(Cipher.AES_128_GCM, Auth.SHA_256, Key.RSA_4096);

    @NonNull
    private final Cipher cipher;
    @NonNull
    private final Auth auth;
    @NonNull
    private final Key key;

    TunnelMode(@NonNull Cipher cipher, @NonNull Auth auth, @NonNull Key key) {
        this.cipher = cipher;
        this.auth = auth;
        this.key = key;
    }

    @NonNull
    public Cipher cipher() {
        return cipher;
    }

    @NonNull
    public Auth auth() {
        return auth;
    }

    @NonNull
    public Key key() {
        return key;
    }

    @NonNull
    public static TunnelMode find(String name) {
        for (TunnelMode tunnelMode : values()) {
            if (tunnelMode.name().equals(name)) {
                return tunnelMode;
            }
        }
        return RECOMMENDED;
    }

    public enum Cipher {
        NONE("none"),
        AES_128_GCM("AES-128-GCM"),
        AES_256_GCM("AES-256-GCM");

        @NonNull
        private final String value;

        Cipher(@NonNull String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum Auth {
        SHA_256("SHA-256");

        @NonNull
        private final String value;

        Auth(@NonNull String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public enum Key {
        RSA_4096("RSA-4096");

        @NonNull
        private final String value;

        Key(@NonNull String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
