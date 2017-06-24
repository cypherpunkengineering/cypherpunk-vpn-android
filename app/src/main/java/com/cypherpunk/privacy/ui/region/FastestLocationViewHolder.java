package com.cypherpunk.privacy.ui.region;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cypherpunk.privacy.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * view holder for region
 */
class FastestLocationViewHolder extends RecyclerView.ViewHolder {

    private static final int LAYOUT_ID = R.layout.list_item_vpn_fastest_location;

    @NonNull
    public static FastestLocationViewHolder create(@NonNull LayoutInflater inflater, ViewGroup parent) {
        return new FastestLocationViewHolder(inflater.inflate(LAYOUT_ID, parent, false));
    }

    @BindView(R.id.flag)
    ImageView flagView;

    private FastestLocationViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }
}
