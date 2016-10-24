package com.cypherpunk.android.vpn.data.api.json;


public class StatusResult {

    private final String type;
    private final String renewal;
    private final boolean confirmed;
    private final String expiration;

    public StatusResult(String type, String renewal, boolean confirmed, String expiration) {
        this.type = type;
        this.renewal = renewal;
        this.confirmed = confirmed;
        this.expiration = expiration;
    }

    public String getType() {
        return type;
    }

    public String getRenewal() {
        return renewal;
    }

    public boolean getConfirmed() {
        return confirmed;
    }

    public String getExpiration() {
        return expiration;
    }

    @Override
    public String toString() {
        return "StatusResult{" +
                "type='" + type + '\'' +
                ", renewal='" + renewal + '\'' +
                ", confirmed='" + confirmed + '\'' +
                ", expiration='" + expiration + '\'' +
                '}';
    }
}
