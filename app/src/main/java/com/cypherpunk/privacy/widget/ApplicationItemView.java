package com.cypherpunk.privacy.widget;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.databinding.ViewApplicationItemBinding;
import com.cypherpunk.privacy.model.AppData;

public class ApplicationItemView extends RelativeLayout implements Checkable, View.OnClickListener {

    private ViewApplicationItemBinding binding;

    public ApplicationItemView(Context context) {
        this(context, null);
    }

    public ApplicationItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ApplicationItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = DataBindingUtil.inflate(
                LayoutInflater.from(context), R.layout.view_application_item, this, true);
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

    public void setApp(AppData app) {
        binding.setApp(app);
    }

    @Override
    public void onClick(View view) {
        toggle();
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        binding.button.setOnCheckedChangeListener(listener);
    }
}
