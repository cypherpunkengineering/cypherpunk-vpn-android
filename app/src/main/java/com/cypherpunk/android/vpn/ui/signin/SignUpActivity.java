package com.cypherpunk.android.vpn.ui.signin;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.cypherpunk.android.vpn.CypherpunkApplication;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.CypherpunkService;
import com.cypherpunk.android.vpn.data.api.json.LoginRequest;
import com.cypherpunk.android.vpn.data.api.json.LoginResult;
import com.cypherpunk.android.vpn.databinding.ActivitySignUpBinding;

import java.net.UnknownHostException;

import javax.inject.Inject;

import retrofit2.adapter.rxjava.HttpException;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


public class SignUpActivity extends AppCompatActivity {

    private static final String EXTRA_EMAIL = "email";
    private ActivitySignUpBinding binding;
    private ProgressFragment dialogFragment;
    private Subscription subscription = Subscriptions.empty();

    @Inject
    CypherpunkService webService;

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull String email) {
        Intent intent = new Intent(context, SignUpActivity.class);
        intent.putExtra(EXTRA_EMAIL, email);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        binding.password.requestFocus();
        binding.password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    signUp();
                    return true;
                }
                return false;
            }
        });

        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        dialogFragment = ProgressFragment.newInstance();
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }

    private void signUp() {
        binding.password.setError(null);

        String password = binding.password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            binding.password.setError(getString(R.string.error_field_required));
            focusView = binding.email;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            // success
            dialogFragment.show(getSupportFragmentManager());

            subscription = webService
                    .signup(new LoginRequest(getIntent().getStringExtra(EXTRA_EMAIL), password))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult result) {
                            dialogFragment.dismiss();
                            // TODO;
                        }

                        @Override
                        public void onError(Throwable error) {
                            dialogFragment.dismiss();

                            error.printStackTrace();
                            if (error instanceof UnknownHostException) {
                                Toast.makeText(SignUpActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                            } else if (error instanceof HttpException) {
                                HttpException httpException = (HttpException) error;
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
