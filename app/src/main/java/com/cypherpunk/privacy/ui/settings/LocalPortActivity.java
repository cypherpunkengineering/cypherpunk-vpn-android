package com.cypherpunk.privacy.ui.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.databinding.ActivityLocalPortBinding;

public class LocalPortActivity extends AppCompatActivity {

    public static String EXTRA_VALUE = "value";

    private ActivityLocalPortBinding binding;

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull String value) {
        Intent intent = new Intent(context, LocalPortActivity.class);
        intent.putExtra(EXTRA_VALUE, value);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_local_port);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_local_port);
            actionBar.setHomeAsUpIndicator(R.drawable.close_vector);
        }

        binding.localPort.setText(getIntent().getStringExtra(EXTRA_VALUE));
        binding.localPort.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.local_port, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_done:
                Intent intent = new Intent();
                intent.putExtra(ListPreferenceActivity.EXTRA_KEY, "vpn_port_local");
                intent.putExtra(ListPreferenceActivity.EXTRA_SELECTED_VALUE, binding.localPort.getText().toString());
                setResult(Activity.RESULT_OK, intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
