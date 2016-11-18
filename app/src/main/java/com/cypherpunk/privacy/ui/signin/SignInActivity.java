package com.cypherpunk.privacy.ui.signin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.UserManager;
import com.cypherpunk.privacy.data.api.json.EmailRequest;
import com.cypherpunk.privacy.data.api.json.LoginRequest;
import com.cypherpunk.privacy.data.api.json.LoginResult;
import com.cypherpunk.privacy.databinding.ActivitySignInBinding;
import com.cypherpunk.privacy.ui.setup.TutorialActivity;

import java.net.UnknownHostException;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.adapter.rxjava.HttpException;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


public class SignInActivity extends AppCompatActivity {

    private static final String EXTRA_EMAIL = "email";

    private ActivitySignInBinding binding;
    private ProgressFragment dialogFragment;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Inject
    CypherpunkService webService;
    private String email;

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull String email) {
        Intent intent = new Intent(context, SignInActivity.class);
        intent.putExtra(EXTRA_EMAIL, email);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        binding.password.requestFocus();
        binding.password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptSignIn();
                    return true;
                }
                return false;
            }
        });


//        if (BuildConfig.DEBUG) {
//            binding.email.setText("test@test.test");
//            binding.password.setText("test123");
//        }
        binding.signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignIn();
            }
        });

        binding.forgotPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                recoverPassword();
            }
        });

        binding.forgotPasswordButton.setPaintFlags(
                binding.forgotPasswordButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        dialogFragment = ProgressFragment.newInstance();

        email = getIntent().getStringExtra(EXTRA_EMAIL);
    }

    @Override
    protected void onDestroy() {
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    private void attemptSignIn() {
        binding.password.setError(null);

        final String password = binding.password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            binding.password.setError(getString(R.string.error_field_required));
            focusView = binding.password;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            dialogFragment.show(getSupportFragmentManager());
            Subscription subscription = webService
                    .login(new LoginRequest(email, password))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult result) {
                            dialogFragment.dismiss();
                            UserManager.saveMailAddress(email);
                            UserManager.savePassword(password);
                            UserManager.saveSecret(result.getSecret());
                            Intent intent = new Intent(SignInActivity.this, TutorialActivity.class);
                            TaskStackBuilder builder = TaskStackBuilder.create(SignInActivity.this);
                            builder.addNextIntent(intent);
                            builder.startActivities();
                            finish();
                        }

                        @Override
                        public void onError(Throwable error) {
                            dialogFragment.dismiss();
                            if (error instanceof UnknownHostException) {
                                Toast.makeText(SignInActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                            } else if (error instanceof HttpException) {
                                HttpException httpException = (HttpException) error;
                                if (httpException.code() == 400) {
                                    Toast.makeText(SignInActivity.this, R.string.invalid_mail_password, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
            subscriptions.add(subscription);
        }
    }

    private void recoverPassword() {
        dialogFragment.show(getSupportFragmentManager());
        Subscription subscription = webService
                .recoverPassword(new EmailRequest(email))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody result) {
                        dialogFragment.dismiss();
                        Toast.makeText(SignInActivity.this, R.string.sign_in_set_password_recovery, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable error) {
                        dialogFragment.dismiss();
                        if (error instanceof UnknownHostException) {
                            Toast.makeText(SignInActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignInActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        subscriptions.add(subscription);
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
