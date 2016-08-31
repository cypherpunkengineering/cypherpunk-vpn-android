package com.cypherpunk.android.vpn.widget;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ViewSettingItemBinding;
import com.cypherpunk.android.vpn.model.SettingItem;

public class SettingItemView extends RelativeLayout implements Checkable {

    private ViewSettingItemBinding binding;

    public SettingItemView(Context context) {
        this(context, null);
    }

    public SettingItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.view_setting_item, this, true);
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
    }

    public void setSetting(SettingItem settingItem) {
        binding.setSetting(settingItem);
    }
}
