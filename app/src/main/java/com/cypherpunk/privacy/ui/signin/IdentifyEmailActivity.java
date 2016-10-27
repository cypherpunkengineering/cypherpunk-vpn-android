package com.cypherpunk.privacy.ui.signin;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.json.IdentifyEmailRequest;
import com.cypherpunk.privacy.databinding.ActivityIdentifyEmailBinding;

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

    private ActivityIdentifyEmailBinding binding;
    private ProgressFragment dialogFragment;
    private Subscription subscription = Subscriptions.empty();

    @Inject
    CypherpunkService webService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_identify_email);
        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        binding.email.requestFocus();
        binding.email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        dialogFragment = ProgressFragment.newInstance();

        // TODO: fragmentにしたい
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }

    private void identifyEmail() {
        binding.email.setError(null);

        final String email = binding.email.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            binding.email.setError(getString(R.string.error_field_required));
            focusView = binding.email;
            cancel = true;
        }

        if (!isValidEmail(email)) {
            binding.email.setError(getString(R.string.error_invalid_email));
            focusView = binding.email;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            dialogFragment.show(getSupportFragmentManager());

            subscription = webService
                    .identifyEmail(new IdentifyEmailRequest(email))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<ResponseBody>() {
                        @Override
                        public void onSuccess(ResponseBody result) {
                            dialogFragment.dismiss();
                            startActivity(SignInActivity.createIntent(IdentifyEmailActivity.this, email));
                        }

                        @Override
                        public void onError(Throwable error) {
                            dialogFragment.dismiss();
                            if (error instanceof UnknownHostException) {
                                Toast.makeText(IdentifyEmailActivity.this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                            } else if (error instanceof HttpException) {
                                HttpException httpException = (HttpException) error;
                                if (httpException.code() == 401 || httpException.code() == 400) {
                                    startActivity(SignUpActivity.createIntent(IdentifyEmailActivity.this, email));
                                }
                            }
                        }
                    });
        }
    }

    public boolean isValidEmail(CharSequence text) {
        return !TextUtils.isEmpty(text) && Patterns.EMAIL_ADDRESS.matcher(text).matches();
    }
}
