package com.cypherpunk.privacy.model;

import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

@RealmClass
public class RealmString implements RealmModel {

    private String val;

    public RealmString() {
    }

    public RealmString(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}