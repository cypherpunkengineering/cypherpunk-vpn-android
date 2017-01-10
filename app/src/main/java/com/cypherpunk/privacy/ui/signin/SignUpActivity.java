package com.cypherpunk.privacy.ui.signin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.TextAppearanceSpan;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.UserManager;
import com.cypherpunk.privacy.data.api.json.AccountStatusResult;
import com.cypherpunk.privacy.data.api.json.SignUpRequest;
import com.cypherpunk.privacy.databinding.ActivitySignUpBinding;

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

    private String email;

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

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

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

        email = getIntent().getStringExtra(EXTRA_EMAIL);

        dialogFragment = ProgressFragment.newInstance();

        setTextStyle();
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }

    private void signUp() {
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
            // success
            dialogFragment.show(getSupportFragmentManager());

            subscription = webService
                    .signup(new SignUpRequest(email, password))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<AccountStatusResult>() {
                        @Override
                        public void onSuccess(AccountStatusResult result) {
                            dialogFragment.dismiss();
                            UserManager.saveMailAddress(email);
                            UserManager.saveSecret(result.getSecret());
                            UserManager.saveVpnUsername(result.getPrivacy().username);
                            UserManager.saveVpnPassword(result.getPrivacy().password);
                            startActivity(ConfirmationEmailActivity.createIntent(SignUpActivity.this, email));
                        }

                        @Override
                        public void onError(Throwable error) {
                            dialogFragment.dismiss();

                            error.printStackTrace();
                            if (error instanceof UnknownHostException) {
                                Toast.makeText(SignUpActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                            } else if (error instanceof HttpException) {
                                HttpException httpException = (HttpException) error;
                                if (httpException.code() == 409) {
                                    Toast.makeText(SignUpActivity.this, R.string.email_already_registered, Toast.LENGTH_SHORT).show();
                                    startActivity(SignInActivity.createIntent(SignUpActivity.this, email));
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

    private void setTextStyle() {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        sb.append(getString(R.string.sign_up_policy1));
        int start = sb.length();
        sb.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_Cypherpunk_Policy), 0, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append(getString(R.string.sign_up_policy2));
        sb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_of_service_url))));
            }
        }, start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_Cypherpunk_PolicyLink), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = sb.length();
        sb.append(getString(R.string.sign_up_policy3));
        sb.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_Cypherpunk_Policy), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = sb.length();
        sb.append(getString(R.string.sign_up_policy4));
        sb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_url))));
            }
        }, start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_Cypherpunk_PolicyLink), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = sb.length();
        sb.append(getString(R.string.sign_up_policy5));
        sb.setSpan(new TextAppearanceSpan(this, R.style.TextAppearance_Cypherpunk_Policy), start, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.text.setText(sb);
        binding.text.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
