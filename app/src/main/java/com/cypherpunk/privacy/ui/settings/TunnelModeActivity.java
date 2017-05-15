package com.cypherpunk.privacy.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.model.CypherpunkSetting.TunnelMode;
import com.cypherpunk.privacy.utils.ResourceUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class TunnelModeActivity extends AppCompatActivity {

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, TunnelModeActivity.class);
    }

    private final List<Checkable> checkableList = new ArrayList<>();
    private CypherpunkSetting cypherpunkSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tunnel_mode);
        ButterKnife.bind(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.close_vector);
        }

        cypherpunkSetting = new CypherpunkSetting();

        final LinearLayout container = ButterKnife.findById(this, R.id.container);
        final LayoutInflater inflater = LayoutInflater.from(this);

        final TunnelMode tunnelMode = cypherpunkSetting.tunnelMode();

        for (final TunnelMode mode : TunnelMode.values()) {
            final View view = inflater.inflate(R.layout.tunnel_mode_item, container, false);
            container.addView(view);

            final Checkable checkable = ButterKnife.findById(view, R.id.checkable);
            checkableList.add(checkable);

            final TextView nameView = ButterKnife.findById(view, R.id.title);
            nameView.setText(ResourceUtil.getTitleFor(mode));

            final TextView summaryView = ButterKnife.findById(view, R.id.summary);
            summaryView.setText(ResourceUtil.getSummaryFor(mode));

            final TextView cypherView = ButterKnife.findById(view, R.id.cipher_value);
            cypherView.setText(mode.cipher().value());

            final TextView authView = ButterKnife.findById(view, R.id.auth_value);
            authView.setText(mode.auth().value());

            final TextView keyView = ButterKnife.findById(view, R.id.key_value);
            keyView.setText(mode.key().value());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!checkable.isChecked()) {
                        for (Checkable c : checkableList) {
                            c.setChecked(false);
                        }
                        checkable.setChecked(true);

                        cypherpunkSetting.updateTunnelMode(mode);
                    }
                }
            });

            container.addView(inflater.inflate(R.layout.divider, container, false));

            if (mode == tunnelMode) {
                checkable.setChecked(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
