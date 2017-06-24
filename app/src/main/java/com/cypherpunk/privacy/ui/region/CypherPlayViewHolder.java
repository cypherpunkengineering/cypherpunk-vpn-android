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
class CypherPlayViewHolder extends RecyclerView.ViewHolder {

    private static final int LAYOUT_ID = R.layout.list_item_vpn_cypherplay;

    @NonNull
    public static CypherPlayViewHolder create(@NonNull LayoutInflater inflater, ViewGroup parent) {
        return new CypherPlayViewHolder(inflater.inflate(LAYOUT_ID, parent, false));
    }

    private CypherPlayViewHolder(View view) {
        super(view);
    }
}
