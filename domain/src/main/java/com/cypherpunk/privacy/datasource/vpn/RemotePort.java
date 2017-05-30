package com.cypherpunk.privacy.datasource.vpn;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

/**
 * remote port
 */
@AutoValue
public abstract class RemotePort {

    @NonNull
    public static RemotePort create(@NonNull Type type, @NonNull Port port) {
        return new AutoValue_RemotePort(type, port);
    }

    @NonNull
    public abstract Type type();

    @NonNull
    public abstract Port port();

    public enum Type {
        UDP,
        TCP;

        @NonNull
        public static Type find(String value) {
            for (Type category : values()) {
                if (category.name().equals(value)) {
                    return category;
                }
            }
            return UDP;
        }
    }

    public enum Port {
        PORT_7133(7133),
        PORT_5060(5060),
        PORT_53(53);

        private final int value;

        Port(int value) {
            this.value = value;
        }

        @NonNull
        public static Port find(int value) {
            for (Port port : values()) {
                if (port.value == value) {
                    return port;
                }
            }
            return PORT_7133;
        }

        public int value() {
            return value;
        }
    }
}
