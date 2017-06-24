package com.cypherpunk.privacy.ui.region;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cypherpunk.privacy.R;

/**
 * view holder for region
 */
class ViewHolderCypherPlay extends RecyclerView.ViewHolder {

    private static final int LAYOUT_ID = R.layout.list_item_vpn_cypherplay;

    @NonNull
    public static ViewHolderCypherPlay create(@NonNull LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolderCypherPlay(inflater.inflate(LAYOUT_ID, parent, false));
    }

    private ViewHolderCypherPlay(View view) {
        super(view);
    }
}
