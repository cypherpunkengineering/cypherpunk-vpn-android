package com.cypherpunk.privacy.ui.account;

import android.support.annotation.NonNull;

public class PurchaseItem {

    public enum Type {
        MONTHLY,
        ANNUALLY
    }

    @NonNull
    private final Type type;
    private final String title;
    private final String description;
    private final String price;
    private final long priceMicros;

    public PurchaseItem(@NonNull Type type, String title, String description, String price, long priceMicros) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.price = price;
        this.priceMicros = priceMicros;
    }

    @NonNull
    public Type type() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public long getPriceMicros() {
        return priceMicros;
    }
}
