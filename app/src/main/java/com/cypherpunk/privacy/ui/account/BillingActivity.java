package com.cypherpunk.privacy.ui.account;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.datasource.account.Subscription;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;
import com.cypherpunk.privacy.ui.common.FullScreenProgressDialog;

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

public abstract class BillingActivity extends AppCompatActivity {

    private FullScreenProgressDialog dialog;
    private Disposable disposable = Disposables.empty();

    @Inject
    NetworkRepository networkRepository;

    @Inject
    AccountSetting accountSetting;

    @BindView(R.id.button_annually)
    TextView annuallyButton;

    @BindView(R.id.button_monthly)
    TextView monthlyButton;

    @BindView(R.id.discount)
    TextView discountView;

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
//        discountView.setVisibility(View.GONE);
    }

    @OnClick(R.id.button_annually)
    void annuallyButtonClicked() {
        purchase(PurchaseItem.Type.ANNUALLY);
    }

    @OnClick(R.id.button_monthly)
    void monthlyButtonClicked() {
        purchase(PurchaseItem.Type.MONTHLY);
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
        super.onDestroy();
    }

    //
    //
    //

    protected abstract void purchase(@NonNull PurchaseItem.Type type);

    protected void onQueryResult(@NonNull List<PurchaseItem> items) {
//        final long annuallyPrice = skuDetailsAnnually.getPriceAmountMicros();
//        final long monthlyPrice = skuDetailsMonthly.getPriceAmountMicros();
//
//        final int percent = (int) (100 * (monthlyPrice - annuallyPrice) / monthlyPrice);
//        discountView.setText("SAVE " + percent + "%");

        for (PurchaseItem item : items) {
            switch (item.type()) {
                case MONTHLY:
                    monthlyButton.setText(item.getPrice() +
                            "/MO\nBilled Monthly");

                    monthlyButton.setVisibility(View.VISIBLE);
//                    discountView.setVisibility(View.VISIBLE);
                    break;

                case ANNUALLY:
                    annuallyButton.setText(item.getPrice() +
                            "/MO\nBilled Annually");
                    annuallyButton.setVisibility(View.VISIBLE);
                    break;
            }
        }

        final Subscription subscription = accountSetting.subscription();
        switch (subscription.type()) {
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

    protected void onPurchaseResult(@NonNull PurchaseResult purchaseResult) {
        if (purchaseResult.isSuccess()) {
            final String accountId = accountSetting.accountId();
            upgradeAccount(accountId == null ? "" : accountId, purchaseResult.getSku(),
                    purchaseResult.getResultJson());
        } else {
            Toast.makeText(BillingActivity.this, "purchase failed", Toast.LENGTH_SHORT).show();
        }
    }

}
