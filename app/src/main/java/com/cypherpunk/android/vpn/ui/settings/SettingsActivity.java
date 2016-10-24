package com.cypherpunk.android.vpn.ui.settings;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivitySettingsBinding;

/**
 * unused
 */
public class SettingsActivity extends AppCompatActivity
        implements SettingConnectDialogFragment.ConnectDialogListener {

    public static final String EXTRA_CONNECT = "connect";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_settings);

            if (getResources().getBoolean(R.bool.is_tablet)) {
                actionBar.setHomeAsUpIndicator(R.drawable.close_vector);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectDialogButtonClick() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONNECT, true);
        setResult(RESULT_OK, intent);
        finish();
    }

}
