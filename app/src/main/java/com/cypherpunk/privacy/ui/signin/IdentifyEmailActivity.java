package com.cypherpunk.privacy.ui.signin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.json.EmailRequest;
import com.cypherpunk.privacy.databinding.ActivityIdentifyEmailBinding;
import com.cypherpunk.privacy.ui.common.FullScreenProgressDialog;

import java.net.UnknownHostException;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.adapter.rxjava.HttpException;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

public class IdentifyEmailActivity extends AppCompatActivity {

    @NonNull
    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, IdentifyEmailActivity.class);
    }

    private ActivityIdentifyEmailBinding binding;
    @Nullable
    private FullScreenProgressDialog dialog;
    @NonNull
    private Subscription subscription = Subscriptions.empty();

    @Inject
    CypherpunkService webService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_identify_email);
        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        binding.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.textInputLayout.setError(null);
                binding.textInputLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    identifyEmail();
                    return true;
                }
                return false;
            }
        });
        binding.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                identifyEmail();
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

    private void identifyEmail() {
        final String email = binding.editText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            binding.textInputLayout.setError(getString(R.string.error_field_required));
            binding.editText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayout.setError(getString(R.string.error_invalid_email));
            binding.editText.requestFocus();
            return;
        }

        dialog = new FullScreenProgressDialog(this);
        dialog.show();

        final Context context = this;

        subscription = webService.identifyEmail(new EmailRequest(email))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<ResponseBody>() {
                    @Override
                    public void onSuccess(ResponseBody result) {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        startActivity(SignInActivity.createIntent(context, email));
                    }

                    @Override
                    public void onError(Throwable error) {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                        if (error instanceof UnknownHostException) {
                            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
                        } else if (error instanceof HttpException) {
                            HttpException httpException = (HttpException) error;
                            if (httpException.code() == 401 || httpException.code() == 400) {
                                startActivity(SignUpActivity.createIntent(context, email));
                            }
                        }
                    }
                });
    }
}
