package com.cypherpunk.android.vpn.ui.settings;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.billing.IabHelper;
import com.cypherpunk.android.vpn.billing.IabResult;
import com.cypherpunk.android.vpn.billing.Inventory;
import com.cypherpunk.android.vpn.billing.Purchase;
import com.cypherpunk.android.vpn.databinding.ActivityUpgradePlanBinding;

public class UpgradePlanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityUpgradePlanBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade_plan);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_upgrade_plan);
        }

        binding.monthlyPlan.setPlan("1 MONTHS", "$ 9.99", false);
        binding.monthly6Plan.setPlan("6 MONTHS", "$ 79.99", false);
        binding.yearlyPlan.setPlan("12 MONTHS", "$ 49.99", true);

        binding.monthly6Plan.setCurrentPlan();

        binding.monthlyPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String payload = "monthly_sample";
                try {
                    helper.launchPurchaseFlow(UpgradePlanActivity.this, SKU_MONTHLY, RC_REQUEST, purchaseFinishedListener, payload);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        });

        setupBilling();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtnaJo8j5g1N40hNEkxUwdP9OQzz/av2MmubCCusUT5aycx7ZSfHt3bipvVmTkFvHFrUvdoAvpZ0xXaKZN3xU+w3dUj4uk32CzHFpC3Rl4vtUN993ZEt0BmJSCvOveCj5JsmeDGe0h6LqAJGHl4RMKFcOoUrK92xhqOJuvO3KfxJcVrDWfGx+luK3pxeqBp2nMZqO3FLyEMsneDOKyZcTgCK9OIPpyjH72aE7cGrJpJUe40iFxV8a2JfOVKFvzRN0gfvKewvik7v4lxD2uEU8g4YV0SEKRYed2xZPvZTiqQF9ar3CcaF3xFJi7a1bEXPMmZ+MF0fiLX+weWi7s8z4RQIDAQAB";
    static final String SKU_MONTHLY = "monthly_sample";
    static final int RC_REQUEST = 10001;

    private IabHelper helper;


    private void setupBilling() {
        helper = new IabHelper(this, PUBLIC_KEY);
        helper.enableDebugLogging(true);
        helper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Toast.makeText(UpgradePlanActivity.this, "Problem setting up in-app billing: " + result, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (helper == null) return;

                try {
                    helper.queryInventoryAsync(gotInventoryFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    IabHelper.QueryInventoryFinishedListener gotInventoryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {

        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {

        }
    };

    IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!helper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
