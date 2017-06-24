package com.cypherpunk.privacy.ui.region;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cypherpunk.privacy.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * view holder for divider
 */
class DividerViewHolder extends RecyclerView.ViewHolder {

    private static final int LAYOUT_ID = R.layout.list_item_vpn_server_divider;

    @NonNull
    public static DividerViewHolder create(@NonNull LayoutInflater inflater, ViewGroup parent) {
        return new DividerViewHolder(inflater.inflate(LAYOUT_ID, parent, false));
    }

    @BindView(R.id.text)
    TextView textView;

    private DividerViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        ViewCompat.setBackground(textView, new RotateGradientDrawable());
    }
}
