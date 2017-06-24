package com.cypherpunk.privacy.ui.region;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.ui.common.FlagView;
import com.cypherpunk.privacy.ui.common.RegionBadgeView;

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
    FlagView flagView;

    @BindView(R.id.name)
    TextView nameView;

    @BindView(R.id.tag)
    RegionBadgeView tagView;

    @BindView(R.id.latency)
    TextView latencyView;

    @BindView(R.id.latency_error)
    View latencyErrorView;

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
            if (TextUtils.isEmpty(latencyView.getText())) {
                latencyErrorView.setVisibility(View.VISIBLE);
            } else {
                latencyView.setVisibility(View.VISIBLE);
            }
        } else {
            flagView.setAlpha(0.3f);
            nameView.setAlpha(0.3f);
            tagView.setAlpha(0.3f);
            latencyView.setVisibility(View.GONE);
            latencyErrorView.setVisibility(View.GONE);
        }
    }
}
