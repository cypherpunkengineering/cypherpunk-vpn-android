package com.cypherpunk.privacy.ui.account;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.billing.IabHelper;
import com.cypherpunk.privacy.billing.IabResult;
import com.cypherpunk.privacy.billing.Inventory;
import com.cypherpunk.privacy.billing.Purchase;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.CypherpunkService;
import com.cypherpunk.privacy.domain.model.Subscription;
import com.cypherpunk.privacy.domain.repository.retrofit.requst.UpgradeAccountRequest;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;
import com.cypherpunk.privacy.databinding.ActivityUpgradePlanBinding;
import com.cypherpunk.privacy.model.UserSetting;
import com.cypherpunk.privacy.widget.ProgressFullScreenDialog;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class UpgradePlanActivity extends AppCompatActivity {

    private ProgressFullScreenDialog dialog;
    private Disposable disposable = Disposables.empty();

    @Inject
    NetworkRepository networkRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

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

        binding.monthlyPlan.setPlan("1 MONTH", "$12.95", false);
        binding.semiannuallyPlan.setPlan("6 MONTHS", "$ 9.99", false);
        binding.annuallyPlan.setPlan("12 MONTHS", "$ 8.32", true);

        final List<String> oldSkus = getOldSkus();

        binding.monthlyPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String payload = "";
                try {
                    helper.launchPurchaseFlow(UpgradePlanActivity.this, SKU_MONTHLY,
                            IabHelper.ITEM_TYPE_SUBS, oldSkus, RC_REQUEST, purchaseFinishedListener,
                            payload);
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
                    helper.launchPurchaseFlow(UpgradePlanActivity.this, SKU_SEMIANNUALLY,
                            IabHelper.ITEM_TYPE_SUBS, oldSkus, RC_REQUEST, purchaseFinishedListener,
                            payload);
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
                    helper.launchPurchaseFlow(UpgradePlanActivity.this, SKU_ANNUALLY,
                            IabHelper.ITEM_TYPE_SUBS, oldSkus, RC_REQUEST, purchaseFinishedListener,
                            payload);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        });

        final Subscription plan = UserSetting.instance().subscriptionPlan();
        switch (plan.renewal()) {
            case NONE:
                break;
            case MONTHLY:
                binding.monthlyPlan.setCurrentPlan();
                break;
            case ANNUALLY:
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

    @Override
    protected void onDestroy() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        disposable.dispose();
        super.onDestroy();
    }

    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjG76qnaQN3mpl2g5CqND9KIm5oKkKt9vb7bW2i8+Si/8FI2yQKTaKnkGtOxRRNhy0y50S2oNFyuasxWFLHtDCHpodVI9rvJ5zAc+z79Qxrgke1SMzDU1z+oSf3/HWa2yVcAVyBolbvLtras7TXCsKIIWaXbMwccN3L2tW0kZkNkGryqlJJ0Nw/zGCmOY6t5hDZ5Ogh4avlND14naO4P4OqtE0eJh5BJ8WQFUe5mHvp8QLOsN0E6hUr2kf7pLMi9MZ3CR9fFvIk9phiPiB8vDD35c4b22SD5EcWgJCIiIVI6IPhg3cJo4H8ZnKd0O6xmEvAal7YRScGQRMcp6aZLu3wIDAQAB";
    static final String SKU_MONTHLY = "monthly1295";
    static final String SKU_SEMIANNUALLY = "semiannually5995";
    static final String SKU_ANNUALLY = "annually9995";
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
            if (result.isSuccess()) {
                upgradeAccount(info.getDeveloperPayload(), info.getSku(), info.getOriginalJson());
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!helper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * @param accountId    developerPayload
     * @param planId       Item Id (monthly899, semiannually4499, annually5999)
     * @param purchaseData INAPP_PURCHASE_DATA
     */
    private void upgradeAccount(@NonNull String accountId, @NonNull String planId,
                                @NonNull String purchaseData) {

        if (dialog != null) {
            dialog.dismiss();
        }

        dialog = new ProgressFullScreenDialog(this);
        dialog.setCancelable(false);

        disposable = networkRepository
                .upgradeAccount(accountId, planId, purchaseData)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<StatusResult>() {
                    @Override
                    public void onSuccess(StatusResult accountStatus) {
                        UserSetting.instance().updateStatus(accountStatus.account.type,
                                accountStatus.subscription.renewal,
                                accountStatus.subscription.expiration);

                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(Throwable error) {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        // TODO: how to behave? show toast?
                        finish();
                    }
                });
    }

    @Nullable
    public List<String> getOldSkus() {
        final Subscription plan = UserSetting.instance().subscriptionPlan();
        switch (plan.renewal()) {
            case MONTHLY:
                return Collections.singletonList(SKU_MONTHLY);
            case ANNUALLY:
                return Collections.singletonList(SKU_ANNUALLY);
            default:
                return null;
        }
    }
}
