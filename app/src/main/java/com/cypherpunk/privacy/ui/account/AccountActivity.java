package com.cypherpunk.privacy.ui.account;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.databinding.ActivityAccountBinding;

/**
 * unused
 */
public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityAccountBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_account);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_account);
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

}
