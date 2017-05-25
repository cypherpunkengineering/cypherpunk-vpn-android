package com.cypherpunk.privacy.ui.region;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.widget.RegionTagView;
import com.cypherpunk.privacy.widget.StarView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * view holder for region
 */
class RegionViewHolder extends RecyclerView.ViewHolder {

    private static final int LAYOUT_ID = R.layout.list_item_vpn_server;

    @NonNull
    public static RegionViewHolder create(@NonNull LayoutInflater inflater, ViewGroup parent) {
        return new RegionViewHolder(inflater.inflate(LAYOUT_ID, parent, false));
    }

    @BindView(R.id.flag)
    ImageView flagView;

    @BindView(R.id.name)
    TextView nameView;

    @BindView(R.id.favorite)
    StarView favoriteView;

    @BindView(R.id.tag)
    RegionTagView tagView;

    private RegionViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    void setEnabled(boolean enabled) {
        itemView.setClickable(enabled);
        if (enabled) {
            flagView.setAlpha(1f);
            nameView.setAlpha(1f);
            tagView.setAlpha(1f);
            favoriteView.setAlpha(1f);
        } else {
            flagView.setAlpha(0.5f);
            nameView.setAlpha(0.5f);
            tagView.setAlpha(0.5f);
            favoriteView.setAlpha(0.5f);
        }
    }
}
