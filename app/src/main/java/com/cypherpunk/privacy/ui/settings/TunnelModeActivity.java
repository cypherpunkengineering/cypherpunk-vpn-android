package com.cypherpunk.privacy.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.domain.model.VpnSetting;
import com.cypherpunk.privacy.datasource.vpn.TunnelMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class TunnelModeActivity extends AppCompatActivity {

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, TunnelModeActivity.class);
    }

    private final List<Checkable> checkableList = new ArrayList<>();

    @Inject
    VpnSetting vpnSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tunnel_mode);
        ButterKnife.bind(this);

        CypherpunkApplication.instance.getAppComponent().inject(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_vector);
        }

        final LinearLayout container = ButterKnife.findById(this, R.id.container);
        final LayoutInflater inflater = LayoutInflater.from(this);

        final TunnelMode tunnelMode = vpnSetting.tunnelMode();

        for (final TunnelMode mode : TunnelMode.values()) {
            final View view = inflater.inflate(R.layout.list_item_tunnel_mode, container, false);
            container.addView(view);

            final Checkable checkable = ButterKnife.findById(view, R.id.checkable);
            checkableList.add(checkable);

            if (mode == tunnelMode) {
                checkable.setChecked(true);
            }

            final TextView nameView = ButterKnife.findById(view, R.id.title);
            nameView.setText(getTitleFor(mode));

            final TextView summaryView = ButterKnife.findById(view, R.id.summary);
            summaryView.setText(getSummaryFor(mode));

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

                        update(mode);
                    }
                }
            });
        }
    }

    private void update(@NonNull TunnelMode tunnelMode) {
        vpnSetting.updateTunnelMode(tunnelMode);
        setResult(RESULT_OK);
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

    @StringRes
    public static int getTitleFor(@NonNull TunnelMode mode) {
        switch (mode) {
            case RECOMMENDED:
                return R.string.tunnel_mode_recommended_title;
            case MAX_SPEED:
                return R.string.tunnel_mode_max_speed_title;
            case MAX_PRIVACY:
                return R.string.tunnel_mode_max_privacy_title;
            case MAX_STEALTH:
                return R.string.tunnel_mode_max_stealth_title;
            default:
                throw new IllegalArgumentException();
        }
    }

    @StringRes
    public static int getSummaryFor(@NonNull TunnelMode mode) {
        switch (mode) {
            case RECOMMENDED:
                return R.string.tunnel_mode_recommended_summary;
            case MAX_SPEED:
                return R.string.tunnel_mode_max_speed_summary;
            case MAX_PRIVACY:
                return R.string.tunnel_mode_max_privacy_summary;
            case MAX_STEALTH:
                return R.string.tunnel_mode_max_stealth_summary;
            default:
                throw new IllegalArgumentException();
        }
    }
}
