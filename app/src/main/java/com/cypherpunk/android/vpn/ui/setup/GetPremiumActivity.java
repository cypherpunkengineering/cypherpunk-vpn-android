package com.cypherpunk.android.vpn.ui.setup;

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

        ActivityGetPremiumBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_get_premium);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
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