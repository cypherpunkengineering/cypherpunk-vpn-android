package com.cypherpunk.android.vpn.ui.account;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.databinding.ActivityUpgradePlanBinding;

public class UpgradePlanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityUpgradePlanBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade_plan);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding.freePlan.setPlan("Free", "Limited access or something.", "$ 0");
        binding.monthlyPlan.setPlan("Monthly", "30days of limited data.", "$ 9.99");
        binding.yearlyPlan.setPlan("Yearly", "12 months of unlimited data.", "$ 99.99");

        binding.monthlyPlan.setChecked(true);
        binding.freePlan.setChecked(false);

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
