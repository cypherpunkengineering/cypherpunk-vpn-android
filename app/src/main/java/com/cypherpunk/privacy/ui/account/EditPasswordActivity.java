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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.ui.common.FullScreenProgressDialog;

import java.net.UnknownHostException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

public class EditPasswordActivity extends AppCompatActivity {

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, EditPasswordActivity.class);
    }

    @NonNull
    private Disposable disposable = Disposables.empty();

    @Nullable
    private FullScreenProgressDialog dialog;

    @Inject
    NetworkRepository networkRepository;

    @BindView(R.id.text_input_layout_password)
    TextInputLayout passwordTextInputLayout;

    @BindView(R.id.text_input_layout_confirm_password)
    TextInputLayout confirmPasswordTextInputLayout;

    @BindView(R.id.text_input_layout_current_password)
    TextInputLayout currentPasswordTextInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
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

    @OnTextChanged(R.id.password)
    void onPasswordTextChanged() {
        passwordTextInputLayout.setError(null);
        passwordTextInputLayout.setErrorEnabled(false);
    }

    @OnTextChanged(R.id.confirm_password)
    void onConfirmPasswordTextChanged() {
        confirmPasswordTextInputLayout.setError(null);
        confirmPasswordTextInputLayout.setErrorEnabled(false);
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
                updatePassword();
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

    private void updatePassword() {
        final EditText passwordEditText = passwordTextInputLayout.getEditText();
        assert passwordEditText != null;
        final String newPassword = passwordEditText.getText().toString();

        final EditText confirmPasswordEditText = confirmPasswordTextInputLayout.getEditText();
        assert confirmPasswordEditText != null;
        final String confirmPassword = confirmPasswordEditText.getText().toString();

        final EditText currentPasswordEditText = currentPasswordTextInputLayout.getEditText();
        assert currentPasswordEditText != null;
        final String oldPassword = currentPasswordEditText.getText().toString();

        View focusView = null;

        if (TextUtils.isEmpty(newPassword)) {
            passwordTextInputLayout.setError(getString(R.string.error_field_required));
            focusView = passwordEditText;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordTextInputLayout.setError(getString(R.string.error_field_required));
            if (focusView == null) {
                focusView = confirmPasswordEditText;
            }
        } else if (!TextUtils.equals(newPassword, confirmPassword)) {
            confirmPasswordTextInputLayout.setError(getString(R.string.edit_password_account_error_do_not_match));
            if (focusView == null) {
                focusView = confirmPasswordEditText;
            }
        }

        if (TextUtils.isEmpty(oldPassword)) {
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

        disposable = networkRepository
                .changePassword(oldPassword, newPassword)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
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
