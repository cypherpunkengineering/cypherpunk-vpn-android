package com.cypherpunk.privacy.ui.account;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.billing.IabHelper;
import com.cypherpunk.privacy.billing.IabResult;
import com.cypherpunk.privacy.billing.Inventory;
import com.cypherpunk.privacy.billing.Purchase;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.model.account.Subscription;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;
import com.cypherpunk.privacy.ui.common.FullScreenProgressDialog;
import com.cypherpunk.privacy.widget.PlanView;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class UpgradePlanActivity extends AppCompatActivity {

    private FullScreenProgressDialog dialog;
    private Disposable disposable = Disposables.empty();

    @Inject
    NetworkRepository networkRepository;

    @Inject
    AccountSetting accountSetting;

    @BindView(R.id.monthly_plan)
    PlanView monthlyPlan;

    @BindView(R.id.annually_plan)
    PlanView annuallyPlan;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_plan);
        ButterKnife.bind(this);

        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_vector);
        }

        monthlyPlan.setPlan("1 MONTH", "$12.95", false);
        annuallyPlan.setPlan("12 MONTHS", "$ 8.32", true);

        final Subscription subscription = accountSetting.subscription();
        switch (subscription.renewal()) {
            case NONE:
                break;
            case MONTHLY:
                monthlyPlan.setCurrentPlan();
                break;
            case ANNUALLY:
                monthlyPlan.setVisibility(View.GONE);
                annuallyPlan.setCurrentPlan();
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

    @OnClick(R.id.monthly_plan)
    void onMonthlyPlanClicked() {
        purchase(SKU_MONTHLY);
    }

    @OnClick(R.id.annually_plan)
    void onAnnuallyPlanClicked() {
        purchase(SKU_ANNUALLY);
    }

    private void purchase(@NonNull String sku) {
        try {
            helper.launchPurchaseFlow(this, sku, IabHelper.ITEM_TYPE_SUBS, getOldSkus(),
                    RC_REQUEST, purchaseFinishedListener, accountSetting.accountId());
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjG76qnaQN3mpl2g5CqND9KIm5oKkKt9vb7bW2i8+Si/8FI2yQKTaKnkGtOxRRNhy0y50S2oNFyuasxWFLHtDCHpodVI9rvJ5zAc+z79Qxrgke1SMzDU1z+oSf3/HWa2yVcAVyBolbvLtras7TXCsKIIWaXbMwccN3L2tW0kZkNkGryqlJJ0Nw/zGCmOY6t5hDZ5Ogh4avlND14naO4P4OqtE0eJh5BJ8WQFUe5mHvp8QLOsN0E6hUr2kf7pLMi9MZ3CR9fFvIk9phiPiB8vDD35c4b22SD5EcWgJCIiIVI6IPhg3cJo4H8ZnKd0O6xmEvAal7YRScGQRMcp6aZLu3wIDAQAB";
    static final String SKU_MONTHLY = "monthly1295";
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

        dialog = new FullScreenProgressDialog(this);
        dialog.setCancelable(false);

        disposable = networkRepository
                .upgradeAccount(accountId, planId, purchaseData)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<StatusResult>() {
                    @Override
                    public void onSuccess(StatusResult accountStatus) {
                        accountSetting.updateAccount(accountStatus.account);
                        accountSetting.updateSubscription(accountStatus.subscription);

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
        final Subscription subscription = accountSetting.subscription();
        switch (subscription.renewal()) {
            case MONTHLY:
                return Collections.singletonList(SKU_MONTHLY);
            case ANNUALLY:
                return Collections.singletonList(SKU_ANNUALLY);
            default:
                return null;
        }
    }
}
