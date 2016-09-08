package com.cypherpunk.android.vpn.ui.signin;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Bundle;
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

import com.cypherpunk.android.vpn.BuildConfig;
import com.cypherpunk.android.vpn.CypherpunkApplication;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.CypherpunkService;
import com.cypherpunk.android.vpn.data.api.UserManager;
import com.cypherpunk.android.vpn.data.api.json.LoginRequest;
import com.cypherpunk.android.vpn.databinding.ActivitySignInBinding;
import com.cypherpunk.android.vpn.ui.main.MainActivity;

import java.net.UnknownHostException;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.adapter.rxjava.HttpException;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private ProgressFragment dialogFragment;
    private Subscription subscription = Subscriptions.empty();

    @Inject
    CypherpunkService webService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_sign_in);
        }

        binding.email.requestFocus();
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

        if (BuildConfig.DEBUG) {
            binding.email.setText("test@test.test");
            binding.password.setText("test123");
        }
        binding.signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignIn();
            }
        });
        binding.moveSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });

        binding.forgotPasswordButton.setPaintFlags(
                binding.forgotPasswordButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        dialogFragment = ProgressFragment.newInstance();
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }

    private void attemptSignIn() {
        binding.email.setError(null);
        binding.password.setError(null);

        final String email = binding.email.getText().toString();
        final String password = binding.password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            binding.password.setError(getString(R.string.error_field_required));
            focusView = binding.password;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            binding.email.setError(getString(R.string.error_field_required));
            focusView = binding.email;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            dialogFragment.show(getSupportFragmentManager());

            subscription = webService
                    .login(new LoginRequest(email, password))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<ResponseBody>() {
                        @Override
                        public void onSuccess(ResponseBody value) {
                            dialogFragment.dismiss();
                            UserManager manager = UserManager.getInstance(SignInActivity.this);
                            manager.saveMailAddress(email);
                            manager.savePassword(password);
                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
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
        }
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
