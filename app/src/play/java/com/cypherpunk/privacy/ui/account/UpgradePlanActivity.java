package com.cypherpunk.privacy.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.cypherpunk.privacy.BuildConfig;
import com.cypherpunk.privacy.billing.IabHelper;
import com.cypherpunk.privacy.billing.IabResult;
import com.cypherpunk.privacy.billing.Inventory;
import com.cypherpunk.privacy.billing.Purchase;
import com.cypherpunk.privacy.billing.SkuDetails;
import com.cypherpunk.privacy.datasource.account.Subscription;

import java.util.ArrayList;
import java.util.List;

public class UpgradePlanActivity extends BillingActivity {

    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjG76qnaQN3mpl2g5CqND9KIm5oKkKt9vb7bW2i8+Si/8FI2yQKTaKnkGtOxRRNhy0y50S2oNFyuasxWFLHtDCHpodVI9rvJ5zAc+z79Qxrgke1SMzDU1z+oSf3/HWa2yVcAVyBolbvLtras7TXCsKIIWaXbMwccN3L2tW0kZkNkGryqlJJ0Nw/zGCmOY6t5hDZ5Ogh4avlND14naO4P4OqtE0eJh5BJ8WQFUe5mHvp8QLOsN0E6hUr2kf7pLMi9MZ3CR9fFvIk9phiPiB8vDD35c4b22SD5EcWgJCIiIVI6IPhg3cJo4H8ZnKd0O6xmEvAal7YRScGQRMcp6aZLu3wIDAQAB";
    private static final String SKU_MONTHLY = "monthly1295";
    private static final String SKU_ANNUALLY = "annually9995";
    private static final int RC_REQUEST = 10001;

    private IabHelper helper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                    subscriptionSkus.add(SKU_ANNUALLY);
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

            final List<PurchaseItem> items = new ArrayList<>();

            final SkuDetails skuDetailsAnnually = inv.getSkuDetails(SKU_ANNUALLY);
            final SkuDetails skuDetailsMonthly = inv.getSkuDetails(SKU_MONTHLY);

            if (skuDetailsMonthly != null) {
                items.add(new PurchaseItem(PurchaseItem.Type.MONTHLY,
                        skuDetailsMonthly.getTitle(),
                        skuDetailsMonthly.getDescription(),
                        skuDetailsMonthly.getPrice()));
            }

            if (skuDetailsAnnually != null) {
                items.add(new PurchaseItem(PurchaseItem.Type.ANNUALLY,
                        skuDetailsAnnually.getTitle(),
                        skuDetailsAnnually.getDescription(),
                        skuDetailsAnnually.getPrice()));
            }

            onQueryResult(items);
        }
    };


    @Override
    protected void purchase(@NonNull PurchaseItem.Type type) {
        final Subscription subscription = accountSetting.subscription();
        List<String> oldSkus = null;
        switch (subscription.type()) {
            case MONTHLY:
                oldSkus = new ArrayList<>();
                oldSkus.add(SKU_MONTHLY);
                break;
            case ANNUALLY:
                oldSkus = new ArrayList<>();
                oldSkus.add(SKU_ANNUALLY);
                break;
        }

        final String sku;
        switch (type) {
            case MONTHLY:
                sku = SKU_MONTHLY;
                break;
            case ANNUALLY:
                sku = SKU_ANNUALLY;
                break;
            default:
                throw new IllegalStateException();
        }

        final String payload = accountSetting.accountId();
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
                onPurchaseResult(new PurchaseResult(true, info.getSku(), info.getOriginalJson(), "success"));
            } else {
                onPurchaseResult(new PurchaseResult(false, info.getSku(), info.getOriginalJson(), "failed"));
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (helper != null) {
            helper.disposeWhenFinished();
            helper = null;
        }
        super.onDestroy();
    }
}
