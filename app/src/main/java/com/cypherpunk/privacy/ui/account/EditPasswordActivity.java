package com.cypherpunk.privacy.ui.account;

import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.UserManager;
import com.cypherpunk.privacy.data.api.json.ChangePasswordRequest;
import com.cypherpunk.privacy.data.api.json.LoginRequest;
import com.cypherpunk.privacy.data.api.json.LoginResult;
import com.cypherpunk.privacy.databinding.ActivityEditPasswordBinding;
import com.cypherpunk.privacy.ui.signin.ProgressFragment;

import java.net.UnknownHostException;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class EditPasswordActivity extends AppCompatActivity {

    private ActivityEditPasswordBinding binding;
    private ProgressFragment dialogFragment = ProgressFragment.newInstance();
    private Subscription subscription = Subscriptions.empty();

    @Inject
    CypherpunkService webService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_password);

        setSupportActionBar(binding.toolbar.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            binding.toolbar.title.setText(R.string.title_activity_edit_password);
            actionBar.setHomeAsUpIndicator(R.drawable.close_vector);
        }

        binding.password.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_done:
                if (validatePassword()) {
                    updatePassword(binding.currentPassword.getText().toString(),
                            binding.password.getText().toString());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }

    private boolean validatePassword() {
        binding.password.setError(null);
        binding.confirmEmail.setError(null);
        binding.currentPassword.setError(null);

        String newPassword = binding.password.getText().toString();
        String confirmPassword = binding.confirmEmail.getText().toString();
        String currentPassword = binding.currentPassword.getText().toString();

        boolean result = true;
        if (TextUtils.isEmpty(newPassword)) {
            binding.password.setError(getString(R.string.error_field_required));
            result = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.confirmEmail.setError(getString(R.string.error_field_required));
            result = false;
        }

        if (TextUtils.isEmpty(currentPassword)) {
            binding.currentPassword.setError(getString(R.string.error_field_required));
            result = false;
        }

        if (!newPassword.equals(confirmPassword)) {
            binding.confirmEmail.setError(getString(R.string.edit_password_account_error_do_not_match));
            result = false;
        }

        return result;
    }

    private void updatePassword(@NonNull final String oldPassword, @NonNull final String newPassword) {
        dialogFragment.show(getSupportFragmentManager());

        subscription = webService
                .login(new LoginRequest(UserManager.getMailAddress(), UserManager.getPassword()))
                .flatMap(new Func1<LoginResult, Single<ResponseBody>>() {
                    @Override
                    public Single<ResponseBody> call(LoginResult result) {
                        return webService.changePassword(new ChangePasswordRequest(oldPassword, newPassword));
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody result) {
                        dialogFragment.dismiss();
                        UserManager.savePassword(newPassword);
                        finish();
                    }

                    @Override
                    public void onError(Throwable error) {
                        dialogFragment.dismiss();
                        if (error instanceof UnknownHostException) {
                            Toast.makeText(EditPasswordActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditPasswordActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
