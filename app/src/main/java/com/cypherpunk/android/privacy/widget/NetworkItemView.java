package com.cypherpunk.android.privacy.widget;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.cypherpunk.android.privacy.R;
import com.cypherpunk.android.privacy.databinding.ViewNetworkItemBinding;
import com.cypherpunk.android.privacy.model.Network;

public class NetworkItemView extends RelativeLayout implements Checkable, View.OnClickListener {

    private ViewNetworkItemBinding binding;

    public NetworkItemView(Context context) {
        this(context, null);
    }

    public NetworkItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.view_network_item, this, true);
        setClickable(true);
        setOnClickListener(this);
    }

    @Override
    public void setChecked(boolean checked) {
        binding.button.setChecked(checked);
    }

    @Override
    public boolean isChecked() {
        return binding.button.isChecked();
    }


    @Override
    public void toggle() {
        binding.button.toggle();
    }

    public void setNetwork(Network network) {
        binding.setNetwork(network);
    }

    @Override
    public void onClick(View view) {
        toggle();
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        binding.button.setOnCheckedChangeListener(listener);
    }
}
