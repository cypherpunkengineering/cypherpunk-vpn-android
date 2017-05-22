package com.cypherpunk.privacy.ui.account;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.json.ChangeEmailRequest;
import com.cypherpunk.privacy.model.UserSetting;
import com.cypherpunk.privacy.ui.common.FullScreenProgressDialog;

import java.net.UnknownHostException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import okhttp3.ResponseBody;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class EditEmailActivity extends AppCompatActivity {

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, EditEmailActivity.class);
    }

    @NonNull
    private Subscription subscription = Subscriptions.empty();

    @Nullable
    private FullScreenProgressDialog dialog;

    @Inject
    CypherpunkService webService;

    @BindView(R.id.text_input_layout_email)
    TextInputLayout emailTextInputLayout;

    @BindView(R.id.text_input_layout_confirm_email)
    TextInputLayout confirmEmailTextInputLayout;

    @BindView(R.id.text_input_layout_current_password)
    TextInputLayout currentPasswordTextInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_email);
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
            actionBar.setHomeAsUpIndicator(R.drawable.close_vector);
        }
    }

    @OnTextChanged(R.id.email)
    void onEmailTextChanged() {
        emailTextInputLayout.setError(null);
        emailTextInputLayout.setErrorEnabled(false);
    }

    @OnTextChanged(R.id.confirm_email)
    void onConfirmEmailTextChanged() {
        confirmEmailTextInputLayout.setError(null);
        confirmEmailTextInputLayout.setErrorEnabled(false);
    }

    @OnTextChanged(R.id.current_password)
    void onCurrentPasswordTextChanged() {
        currentPasswordTextInputLayout.setError(null);
        currentPasswordTextInputLayout.setErrorEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_done:
                updateEmail();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void updateEmail() {
        final EditText emailEditText = emailTextInputLayout.getEditText();
        assert emailEditText != null;
        final String email = emailEditText.getText().toString();

        final EditText confirmEmailEditText = confirmEmailTextInputLayout.getEditText();
        assert confirmEmailEditText != null;
        final String confirmEmail = confirmEmailEditText.getText().toString();

        final EditText currentPasswordEditText = currentPasswordTextInputLayout.getEditText();
        assert currentPasswordEditText != null;
        final String currentPassword = currentPasswordEditText.getText().toString();

        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            emailTextInputLayout.setError(getString(R.string.error_field_required));
            focusView = emailEditText;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTextInputLayout.setError(getString(R.string.error_invalid_email));
            focusView = emailEditText;
        }

        if (TextUtils.isEmpty(confirmEmail)) {
            confirmEmailTextInputLayout.setError(getString(R.string.error_field_required));
            if (focusView == null) {
                focusView = confirmEmailEditText;
            }
        } else if (!TextUtils.equals(email, confirmEmail)) {
            confirmEmailTextInputLayout.setError(getString(R.string.edit_email_account_error_do_not_match));
            if (focusView == null) {
                focusView = confirmEmailEditText;
            }
        }

        if (TextUtils.isEmpty(currentPassword)) {
            currentPasswordTextInputLayout.setError(getString(R.string.error_field_required));
            if (focusView == null) {
                focusView = currentPasswordEditText;
            }
        }

        if (focusView != null) {
            focusView.requestFocus();
            return;
        }

        dialog = new FullScreenProgressDialog(this);
        dialog.show();

        final Context context = this;

        subscription = webService.changeEmail(new ChangeEmailRequest(email, currentPassword))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody result) {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }

                        UserSetting.instance().updateMail(email);

                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(Throwable error) {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        if (error instanceof UnknownHostException) {
                            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, R.string.api_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
