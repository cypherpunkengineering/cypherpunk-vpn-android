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
import android.widget.TextView;
import android.widget.Toast;

import com.cypherpunk.privacy.BuildConfig;
import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.billing.IabHelper;
import com.cypherpunk.privacy.billing.IabResult;
import com.cypherpunk.privacy.billing.Inventory;
import com.cypherpunk.privacy.billing.Purchase;
import com.cypherpunk.privacy.billing.SkuDetails;
import com.cypherpunk.privacy.datasource.account.Subscription;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;
import com.cypherpunk.privacy.ui.common.FullScreenProgressDialog;

import java.util.ArrayList;
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

    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjG76qnaQN3mpl2g5CqND9KIm5oKkKt9vb7bW2i8+Si/8FI2yQKTaKnkGtOxRRNhy0y50S2oNFyuasxWFLHtDCHpodVI9rvJ5zAc+z79Qxrgke1SMzDU1z+oSf3/HWa2yVcAVyBolbvLtras7TXCsKIIWaXbMwccN3L2tW0kZkNkGryqlJJ0Nw/zGCmOY6t5hDZ5Ogh4avlND14naO4P4OqtE0eJh5BJ8WQFUe5mHvp8QLOsN0E6hUr2kf7pLMi9MZ3CR9fFvIk9phiPiB8vDD35c4b22SD5EcWgJCIiIVI6IPhg3cJo4H8ZnKd0O6xmEvAal7YRScGQRMcp6aZLu3wIDAQAB";
    private static final String SKU_MONTHLY = "monthly1295";
    private static final String SKU_ANNUALLY = "annually9995";
    private static final int RC_REQUEST = 10001;

    private FullScreenProgressDialog dialog;
    private Disposable disposable = Disposables.empty();
    private IabHelper helper;

    @Inject
    NetworkRepository networkRepository;

    @Inject
    AccountSetting accountSetting;

    @BindView(R.id.button_annually)
    TextView annuallyButton;

    @BindView(R.id.button_monthly)
    TextView monthlyButton;

    @BindView(R.id.save)
    TextView saveView;

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

//        annuallyButton.setVisibility(View.GONE);
//        monthlyButton.setVisibility(View.GONE);
//        saveView.setVisibility(View.GONE);

        helper = new IabHelper(this, PUBLIC_KEY);
        helper.enableDebugLogging(BuildConfig.DEBUG);
        helper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (result.isFailure()) {
                    if (result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE) {
                        helper = null;
                    }
                    finish();
                    Toast.makeText(UpgradePlanActivity.this, "Problem setting up in-app billing: " + result, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (helper == null) {
                    finish();
                    return;
                }

                try {
                    List<String> subscriptionSkus = new ArrayList<>();
                    subscriptionSkus.add(SKU_MONTHLY);
                    helper.queryInventoryAsync(true, null, subscriptionSkus, gotInventoryFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private final IabHelper.QueryInventoryFinishedListener gotInventoryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {

        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            if (result.isFailure()) {
                finish();
                Toast.makeText(UpgradePlanActivity.this, "Problem setting up in-app billing: " + result, Toast.LENGTH_SHORT).show();
                return;
            }
            if (helper == null) {
                finish();
                return;
            }

            final SkuDetails skuDetailsAnnually = inv.getSkuDetails(SKU_ANNUALLY);
            final SkuDetails skuDetailsMonthly = inv.getSkuDetails(SKU_MONTHLY);

            if (skuDetailsAnnually == null || skuDetailsMonthly == null) {
                return;
            }

//            annuallyButton.setVisibility(View.VISIBLE);
//            monthlyButton.setVisibility(View.VISIBLE);
//            saveView.setVisibility(View.VISIBLE);

            final long annuallyPrice = skuDetailsAnnually.getPriceAmountMicros();
            final long monthlyPrice = skuDetailsMonthly.getPriceAmountMicros();

            annuallyButton.setText(skuDetailsAnnually.getPrice() +
                    skuDetailsAnnually.getPriceCurrencyCode() +
                    "/MO\nBilled Annually");

            monthlyButton.setText(skuDetailsMonthly.getPrice() +
                    skuDetailsMonthly.getPriceCurrencyCode() +
                    "/MO\nBilled Monthly");

            final int percent = (int) (100 * (monthlyPrice - annuallyPrice) / monthlyPrice);
            saveView.setText("SAVE " + percent + "%");

            final Subscription subscription = accountSetting.subscription();
            switch (subscription.renewal()) {
                case NONE:
                    break;
                case MONTHLY:
                    monthlyButton.setEnabled(false);
                    break;
                case ANNUALLY:
                    monthlyButton.setEnabled(false);
                    annuallyButton.setEnabled(false);
                    break;
            }
        }
    };

    @OnClick(R.id.button_annually)
    void annuallyButtonClicked() {
        purchase(SKU_ANNUALLY);
    }

    @OnClick(R.id.button_monthly)
    void monthlyButtonClicked() {
        purchase(SKU_MONTHLY);
    }

    private void purchase(@NonNull String sku) {
        final Subscription subscription = accountSetting.subscription();
        List<String> oldSkus = null;
        switch (subscription.renewal()) {
            case MONTHLY:
                oldSkus = new ArrayList<>();
                oldSkus.add(SKU_MONTHLY);
                break;
            case ANNUALLY:
                oldSkus = new ArrayList<>();
                oldSkus.add(SKU_ANNUALLY);
                break;
        }

        final String payload = "";
        try {
            helper.launchPurchaseFlow(this, sku, IabHelper.ITEM_TYPE_SUBS, oldSkus, RC_REQUEST,
                    purchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!helper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private final IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            if (result.isSuccess()) {
                upgradeAccount(info.getDeveloperPayload(), info.getSku(), info.getOriginalJson());
            } else {
                Toast.makeText(UpgradePlanActivity.this, "cannot purchased", Toast.LENGTH_SHORT).show();
            }
        }
    };

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
                    public void onSuccess(StatusResult result) {
                        accountSetting.updateAccount(result.account);
                        accountSetting.updateSubscription(result.subscription);

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
        disposable.dispose();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        if (helper != null) {
            helper.disposeWhenFinished();
            helper = null;
        }
        super.onDestroy();
    }
}
