package com.cypherpunk.privacy.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.RadioButton;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.model.CypherpunkSetting.InternetKillSwitch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InternetKillSwitchActivity extends AppCompatActivity {

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, InternetKillSwitchActivity.class);
    }

    @BindView(R.id.internet_kill_switch_automatic_check)
    RadioButton automaticView;

    @BindView(R.id.internet_kill_switch_off_check)
    RadioButton offView;

    @BindView(R.id.internet_kill_switch_always_on_check)
    RadioButton alwaysOnView;

    private CypherpunkSetting cypherpunkSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_kill_swtich);
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
        updateChecked(cypherpunkSetting.internetKillSwitch());
    }

    private void updateChecked(@NonNull InternetKillSwitch internetKillSwitch) {
        switch (internetKillSwitch) {
            case AUTOMATIC:
                automaticView.setChecked(true);
                offView.setChecked(false);
                alwaysOnView.setChecked(false);
                break;
            case ALWAYS_ON:
                automaticView.setChecked(false);
                offView.setChecked(false);
                alwaysOnView.setChecked(true);
                break;
            case OFF:
            default:
                automaticView.setChecked(false);
                offView.setChecked(true);
                alwaysOnView.setChecked(false);
                break;
        }
    }

    @OnClick(R.id.internet_kill_switch_automatic)
    void onAutomaticSelected() {
        if (!automaticView.isChecked()) {
            updateChecked(InternetKillSwitch.AUTOMATIC);
            cypherpunkSetting.updateInternetKillSwitch(InternetKillSwitch.AUTOMATIC);
        }
    }

    @OnClick(R.id.internet_kill_switch_off)
    void onOffSelected() {
        if (!offView.isChecked()) {
            updateChecked(InternetKillSwitch.OFF);
            cypherpunkSetting.updateInternetKillSwitch(InternetKillSwitch.OFF);
        }
    }

    @OnClick(R.id.internet_kill_switch_always_on)
    void onAlwaysOnSelected() {
        if (!alwaysOnView.isChecked()) {
            updateChecked(InternetKillSwitch.ALWAYS_ON);
            cypherpunkSetting.updateInternetKillSwitch(InternetKillSwitch.ALWAYS_ON);
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
