package com.cypherpunk.android.vpn.model;


import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.Required;

@RealmClass
public class FavoriteRegion  implements RealmModel {

    @PrimaryKey
    @Required
    private String id;

    @SuppressWarnings("unused")
    public FavoriteRegion() {
    }

    public FavoriteRegion(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
