package com.cypherpunk.privacy.ui.startup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;
import com.cypherpunk.privacy.ui.main.Navigator;

import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import timber.log.Timber;

public class ConfirmationEmailActivity extends AppCompatActivity {

    private static final String EXTRA_EMAIL = "email";

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull String email) {
        final Intent intent = new Intent(context, ConfirmationEmailActivity.class);
        intent.putExtra(EXTRA_EMAIL, email);
        return intent;
    }

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    NetworkRepository networkRepository;

    @Inject
    AccountSetting accountSetting;

    @Inject
    Navigator navigator;

    @BindView(R.id.status)
    TextView statusView;

    @BindView(R.id.check_again_button)
    View checkAgainButton;

    @BindView(R.id.resend_button)
    View resendButton;

    @BindView(R.id.progress)
    View progressView;

    private String email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation_email);
        ButterKnife.bind(this);

        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        final TextView mailView = ButterKnife.findById(this, R.id.mail);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        email = getIntent().getStringExtra(EXTRA_EMAIL);
        mailView.setText(email);

        // check if account has been confirmed
        checkAccountConfirmed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            TaskStackBuilder.create(this)
                    .addNextIntent(IdentifyEmailActivity.createIntent(this))
                    .startActivities();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.check_again_button)
    void onCheckAgainButtonClicked() {
        checkAccountConfirmed();
    }

    @OnClick(R.id.resend_button)
    void onResendButtonClicked() {
        resendButton.setEnabled(false);

        final Context context = this;

        disposables.add(networkRepository.resendEmail(email)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        resendButton.setEnabled(true);
                        final String message = getString(R.string.confirmation_email_resend, email);
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        resendButton.setEnabled(true);
                    }
                }));
    }

    /**
     * GET /api/v0/subscription/status at 10 second intervals
     */
    private void checkAccountConfirmed() {
        statusView.setText(R.string.confirmation_email_status1);
        progressView.setVisibility(View.VISIBLE);
        checkAgainButton.setVisibility(View.GONE);
        resendButton.setVisibility(View.GONE);

        final Context context = this;

        disposables.add(networkRepository.accountStatus()
                .map(new Function<StatusResult, Boolean>() {
                    @Override
                    public Boolean apply(StatusResult result) throws Exception {
                        accountSetting.updateSecret(result.secret);
                        accountSetting.updatePrivacy(result.privacy);
                        accountSetting.updateAccount(result.account);
                        accountSetting.updateSubscription(result.subscription);
                        return result.account.confirmed();
                    }
                })
                .onErrorReturn(new Function<Throwable, Boolean>() {
                    @Override
                    public Boolean apply(Throwable throwable) throws Exception {
                        return false;
                    }
                })
                .repeatWhen(new Function<Flowable<Object>, Publisher<Object>>() {
                    @Override
                    public Publisher<Object> apply(Flowable<Object> objectFlowable) throws Exception {
                        return objectFlowable.delay(10, TimeUnit.SECONDS).take(4);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean confirmed) {
                        Timber.d("confirmed = " + confirmed);
                        if (confirmed) {
                            dispose();
                            navigator.tutorialOrPending(context);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {
                        statusView.setText(R.string.confirmation_email_status2);
                        progressView.setVisibility(View.GONE);
                        checkAgainButton.setVisibility(View.VISIBLE);
                        resendButton.setVisibility(View.VISIBLE);
                    }
                }));
    }

    @Override
    protected void onDestroy() {
        disposables.clear();
        super.onDestroy();
    }
}
