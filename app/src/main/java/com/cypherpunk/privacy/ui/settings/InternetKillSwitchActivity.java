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
import android.view.View;
import android.widget.Checkable;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.model.CypherpunkSetting;
import com.cypherpunk.privacy.model.CypherpunkSetting.InternetKillSwitch;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class InternetKillSwitchActivity extends AppCompatActivity {

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, InternetKillSwitchActivity.class);
    }

    private final List<Checkable> checkableList = new ArrayList<>();
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
        final InternetKillSwitch internetKillSwitch = cypherpunkSetting.internetKillSwitch();

        for (InternetKillSwitch killSwitch : InternetKillSwitch.values()) {
            final int resId;
            switch (killSwitch) {
                case AUTOMATIC:
                    resId = R.id.internet_kill_switch_automatic;
                    break;
                case ALWAYS_ON:
                    resId = R.id.internet_kill_switch_always_on;
                    break;
                case OFF:
                default:
                    resId = R.id.internet_kill_switch_off;
                    break;
            }

            final View view = ButterKnife.findById(this, resId);
            final Checkable checkable = ButterKnife.findById(view, R.id.checkable);
            checkableList.add(checkable);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!checkable.isChecked()) {
                        for (Checkable c : checkableList) {
                            c.setChecked(false);
                        }
                        checkable.setChecked(true);

                        switch (resId) {
                            case R.id.internet_kill_switch_automatic:
                                update(InternetKillSwitch.AUTOMATIC);
                                break;
                            case R.id.internet_kill_switch_off:
                                update(InternetKillSwitch.OFF);
                                break;
                            case R.id.internet_kill_switch_always_on:
                                update(InternetKillSwitch.ALWAYS_ON);
                        }
                    }
                }
            });

            if (internetKillSwitch == killSwitch) {
                checkable.setChecked(true);
            }
        }
    }

    private void update(@NonNull InternetKillSwitch internetKillSwitch) {
        cypherpunkSetting.updateInternetKillSwitch(internetKillSwitch);
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
}
