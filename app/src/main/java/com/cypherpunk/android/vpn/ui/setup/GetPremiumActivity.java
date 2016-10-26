package com.cypherpunk.android.vpn.ui.setup;

import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityGetPremiumBinding;


public class GetPremiumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        ActivityGetPremiumBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_get_premium);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.close_vector);

            if (!getResources().getBoolean(R.bool.is_tablet)) {
                binding.toolbar.title.setText(R.string.title_activity_get_premium);
            }
        }

        binding.monthPlan.setPlan("1 MONTH", "$ 9.99", false);
        binding.sixMonthPlan.setPlan("6 MONTH", "$ 9.99", false);
        binding.yearlyPlan.setPlan("12 MONTH", "$ 9.99", true);
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
