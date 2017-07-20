package com.cypherpunk.privacy.ui.startup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.domain.model.AccountSetting;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;
import com.cypherpunk.privacy.ui.common.FullScreenProgressDialog;
import com.cypherpunk.privacy.ui.common.Urls;

import java.net.UnknownHostException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class LoginActivity extends AppCompatActivity {

    private static final String EXTRA_EMAIL = "email";

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull String email) {
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(EXTRA_EMAIL, email);
        return intent;
    }

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Nullable
    private FullScreenProgressDialog dialog;

    @Inject
    NetworkRepository networkRepository;

    @Inject
    AccountSetting accountSetting;

    @BindView(R.id.text_input_layout)
    TextInputLayout textInputLayout;

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
            login();
            return true;
        }
        return false;
    }

    @OnClick(R.id.login_button)
    void onLoginButtonClicked() {
        login();
    }

    private void login() {
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

        disposables.add(networkRepository
                .login(email, password)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<StatusResult>() {
                    @Override
                    public void onSuccess(StatusResult result) {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }

                        accountSetting.updateSecret(result.secret);
                        accountSetting.updateEmail(email);
                        accountSetting.updatePrivacy(result.privacy);

                        TaskStackBuilder.create(context)
                                .addNextIntent(new Intent(context, TutorialActivity.class))
                                .startActivities();
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        if (e instanceof UnknownHostException) {
                            Toast.makeText(context, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
                        } else if (e instanceof HttpException) {
                            final HttpException he = (HttpException) e;
                            if (he.code() == 400) {
                                Toast.makeText(context, R.string.error_password_invalid, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }));
    }

    @OnClick(R.id.forgot_password_button)
    void onForgotPasswordButtonClicked() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Urls.RECOVER)));
    }

    @Override
    protected void onDestroy() {
        disposables.clear();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        super.onDestroy();
    }
}
