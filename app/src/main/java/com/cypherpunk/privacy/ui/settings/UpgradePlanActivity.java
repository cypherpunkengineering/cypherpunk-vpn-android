package com.cypherpunk.privacy.ui.settings;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.billing.IabHelper;
import com.cypherpunk.privacy.billing.IabResult;
import com.cypherpunk.privacy.billing.Inventory;
import com.cypherpunk.privacy.billing.Purchase;
import com.cypherpunk.privacy.databinding.ActivityUpgradePlanBinding;
import com.cypherpunk.privacy.model.UserSettingPref;

public class UpgradePlanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        ActivityUpgradePlanBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_upgrade_plan);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_upgrade_plan);
            actionBar.setHomeAsUpIndicator(R.drawable.close_vector);
        }

        binding.monthlyPlan.setPlan("1 MONTH", "$ 8.99", false);
        binding.semiannuallyPlan.setPlan("6 MONTHS", "$ 7.49", false);
        binding.annuallyPlan.setPlan("12 MONTHS", "$ 4.99", true);

        // TODO:
        binding.monthlyPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String payload = "";
                try {
                    helper.launchPurchaseFlow(UpgradePlanActivity.this, SKU_MONTHLY, RC_REQUEST, purchaseFinishedListener, payload);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        });

        binding.semiannuallyPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String payload = "";
                try {
                    helper.launchPurchaseFlow(UpgradePlanActivity.this, SKU_SEMIANNUALLY, RC_REQUEST, purchaseFinishedListener, payload);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        });

        binding.annuallyPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String payload = "";
                try {
                    helper.launchPurchaseFlow(UpgradePlanActivity.this, SKU_ANNUALLY, RC_REQUEST, purchaseFinishedListener, payload);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        });

        UserSettingPref userSettingPref = new UserSettingPref();
        String renewal = userSettingPref.userStatusRenewal;
        switch (renewal) {
            case "none":
                break;
            case "monthly":
                binding.monthlyPlan.setCurrentPlan();
                break;
            case "semiannually":
                binding.monthlyPlan.setVisibility(View.GONE);
                binding.semiannuallyPlan.setCurrentPlan();
                break;
            case "annually":
                binding.monthlyPlan.setVisibility(View.GONE);
                binding.semiannuallyPlan.setVisibility(View.GONE);
                binding.annuallyPlan.setCurrentPlan();
                break;
        }


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

    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjG76qnaQN3mpl2g5CqND9KIm5oKkKt9vb7bW2i8+Si/8FI2yQKTaKnkGtOxRRNhy0y50S2oNFyuasxWFLHtDCHpodVI9rvJ5zAc+z79Qxrgke1SMzDU1z+oSf3/HWa2yVcAVyBolbvLtras7TXCsKIIWaXbMwccN3L2tW0kZkNkGryqlJJ0Nw/zGCmOY6t5hDZ5Ogh4avlND14naO4P4OqtE0eJh5BJ8WQFUe5mHvp8QLOsN0E6hUr2kf7pLMi9MZ3CR9fFvIk9phiPiB8vDD35c4b22SD5EcWgJCIiIVI6IPhg3cJo4H8ZnKd0O6xmEvAal7YRScGQRMcp6aZLu3wIDAQAB";
    static final String SKU_MONTHLY = "monthly899";
    static final String SKU_SEMIANNUALLY = "semiannually4499";
    static final String SKU_ANNUALLY = "annually5999";
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
