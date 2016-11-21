package com.cypherpunk.privacy.ui.signin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.json.EmailRequest;
import com.cypherpunk.privacy.data.api.json.StatusResult;
import com.cypherpunk.privacy.databinding.ActivityConfirmationEmailBinding;
import com.cypherpunk.privacy.ui.setup.TutorialActivity;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class ConfirmationEmailActivity extends AppCompatActivity {

    private static final String EXTRA_EMAIL = "email";

    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    CypherpunkService webService;

    private String email;

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull String email) {
        Intent intent = new Intent(context, ConfirmationEmailActivity.class);
        intent.putExtra(EXTRA_EMAIL, email);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        ActivityConfirmationEmailBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_confirmation_email);

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfirmationEmailActivity.this, IdentifyEmailActivity.class);
                TaskStackBuilder builder = TaskStackBuilder.create(ConfirmationEmailActivity.this);
                builder.addNextIntent(intent);
                builder.startActivities();
            }
        });

        email = getIntent().getStringExtra(EXTRA_EMAIL);

        binding.resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendEmail();
            }
        });

        // check if account has been confirmed
        checkAccountConfirmed();

        binding.resendButton.setPaintFlags(
                binding.resendButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
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
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    private void resendEmail() {
        Subscription subscription = webService
                .resendEmail(new EmailRequest(email))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody result) {
                        Toast.makeText(ConfirmationEmailActivity.this, R.string.confirmation_email_resend, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable error) {
                    }
                });
        subscriptions.add(subscription);
    }

    /**
     * GET /api/v0/subscription/status at 10 second intervals
     */
    private void checkAccountConfirmed() {
        Subscription subscription = webService
                .getStatusObservable()
                .repeatWhen(new Func1<Observable<? extends Void>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Void> observable) {
                        return observable.delay(10, TimeUnit.SECONDS);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<StatusResult>() {
                    @Override
                    public void onCompleted() {
                        Intent intent = new Intent(ConfirmationEmailActivity.this, TutorialActivity.class);
                        TaskStackBuilder builder = TaskStackBuilder.create(ConfirmationEmailActivity.this);
                        builder.addNextIntent(intent);
                        builder.startActivities();
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(StatusResult result) {
                        if (result.getConfirmed()) {
                            onCompleted();
                        }
                    }
                });
        subscriptions.add(subscription);
    }
}
