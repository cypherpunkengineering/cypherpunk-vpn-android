package com.cypherpunk.android.vpn.ui;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cypherpunk.android.vpn.BuildConfig;
import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.data.api.CypherpunkClient;
import com.cypherpunk.android.vpn.data.api.UserManager;
import com.cypherpunk.android.vpn.data.api.model.LoginRequest;
import com.cypherpunk.android.vpn.databinding.ActivitySignInBinding;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.adapter.rxjava.HttpException;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


public class SignInActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_GET_ACCOUNTS = 0;

    private ActivitySignInBinding binding;
    private ProgressDialog progressDialog;
    private Subscription subscription = Subscriptions.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
// Contacts permissions
//        mayRequestContacts();

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

        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        populateAutoComplete();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // permissions have been denied.
    }

    private void mayRequestContacts() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            populateAutoComplete();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.permission_rationale),
                    REQUEST_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
        }
    }

    private void populateAutoComplete() {
        ArrayList<String> mailList = new ArrayList<>();
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            String type = account.type;
            if (type.equals("com.google")) {
                String mail = account.name;
                mailList.add(mail);
            }
        }
        addEmailsToAutoComplete(mailList);
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(SignInActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        binding.email.setAdapter(adapter);
        binding.email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                showAccounts();
            }
        });
        showAccounts();
    }

    private void showAccounts() {
        if (binding.email.hasFocus() & ViewCompat.isAttachedToWindow(binding.email)) {
            binding.email.showDropDown();
        }
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
            progressDialog.show();
            subscription = new CypherpunkClient().getApi()
                    .login(new LoginRequest(email, password))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<ResponseBody>() {
                        @Override
                        public void onSuccess(ResponseBody value) {
                            progressDialog.dismiss();
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
                            progressDialog.dismiss();
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
}
