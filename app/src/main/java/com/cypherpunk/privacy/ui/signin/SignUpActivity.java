package com.cypherpunk.privacy.ui.signin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.TextAppearanceSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.UserManager;
import com.cypherpunk.privacy.data.api.json.AccountStatusResult;
import com.cypherpunk.privacy.data.api.json.SignUpRequest;
import com.cypherpunk.privacy.ui.common.FullScreenProgressDialog;
import com.cypherpunk.privacy.ui.common.Urls;

import java.net.UnknownHostException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import retrofit2.adapter.rxjava.HttpException;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

public class SignUpActivity extends AppCompatActivity {

    private static final String EXTRA_EMAIL = "email";

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull String email) {
        final Intent intent = new Intent(context, SignUpActivity.class);
        intent.putExtra(EXTRA_EMAIL, email);
        return intent;
    }

    @NonNull
    private Subscription subscription = Subscriptions.empty();

    @Nullable
    private FullScreenProgressDialog dialog;

    @Inject
    CypherpunkService webService;

    @BindView(R.id.text_input_layout)
    TextInputLayout textInputLayout;

    private String email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        final Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        final TextView policyView = ButterKnife.findById(this, R.id.policy);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        email = getIntent().getStringExtra(EXTRA_EMAIL);

        // set policy text
        policyView.setText(createPolicyText());
        policyView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnTextChanged(R.id.edit_text)
    void onTextChanged() {
        textInputLayout.setError(null);
        textInputLayout.setErrorEnabled(false);
    }

    @OnEditorAction(R.id.edit_text)
    boolean onEditorAction(int id) {
        if (id == EditorInfo.IME_ACTION_DONE) {
            signUp();
            return true;
        }
        return false;
    }

    @OnClick(R.id.sign_up_button)
    void onSignInButtonClicked() {
        signUp();
    }

    private void signUp() {
        final EditText editText = textInputLayout.getEditText();
        assert editText != null;
        final String password = editText.getText().toString();

        if (TextUtils.isEmpty(password)) {
            textInputLayout.setError(getString(R.string.error_field_required));
            editText.requestFocus();
            return;
        }

        dialog = new FullScreenProgressDialog(this);
        dialog.show();

        final Context context = this;

        subscription = webService
                .signup(new SignUpRequest(email, password))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<AccountStatusResult>() {
                    @Override
                    public void onSuccess(AccountStatusResult result) {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }

                        UserManager.saveMailAddress(email);
                        UserManager.saveSecret(result.getSecret());
                        UserManager.saveVpnUsername(result.getPrivacy().username);
                        UserManager.saveVpnPassword(result.getPrivacy().password);

                        startActivity(ConfirmationEmailActivity.createIntent(context, email));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        if (e instanceof UnknownHostException) {
                            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
                        } else if (e instanceof HttpException) {
                            final HttpException he = (HttpException) e;
                            if (he.code() == 409) {
                                Toast.makeText(context, R.string.email_already_registered, Toast.LENGTH_SHORT).show();
                                startActivity(LoginActivity.createIntent(context, email));
                            }
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        super.onDestroy();
    }

    private SpannableStringBuilder createPolicyText() {
        final TextAppearanceSpan linkTextAppearanceSpan = new TextAppearanceSpan(this,
                R.style.TextAppearance_Cypherpunk_PolicyLink);

        final SpannableStringBuilder sb = new SpannableStringBuilder();

        sb.append(getString(R.string.sign_up_policy1));

        {
            int start = sb.length();
            sb.append(getString(R.string.sign_up_policy2));
            int end = sb.length();

            sb.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Urls.TERMS_OF_SERVICE)));
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(linkTextAppearanceSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        sb.append(getString(R.string.sign_up_policy3));

        {
            int start = sb.length();
            sb.append(getString(R.string.sign_up_policy4));
            int end = sb.length();

            sb.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Urls.PRIVACY_POLICY)));
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(linkTextAppearanceSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        sb.append(getString(R.string.sign_up_policy5));

        return sb;
    }
}
