package com.cypherpunk.privacy.ui.startup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.cypherpunk.privacy.CypherpunkApplication;
import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.json.EmailRequest;
import com.cypherpunk.privacy.ui.common.FullScreenProgressDialog;

import java.net.UnknownHostException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
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

    @NonNull
    private Subscription subscription = Subscriptions.empty();

    @Nullable
    private FullScreenProgressDialog dialog;

    @Inject
    CypherpunkService webService;

    @BindView(R.id.text_input_layout)
    TextInputLayout textInputLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_email);
        ButterKnife.bind(this);

        ((CypherpunkApplication) getApplication()).getAppComponent().inject(this);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @OnTextChanged(R.id.edit_text)
    void onTextChanged() {
        textInputLayout.setError(null);
        textInputLayout.setErrorEnabled(false);
    }

    @OnEditorAction(R.id.edit_text)
    boolean onEditorAction(int id) {
        if (id == EditorInfo.IME_ACTION_DONE) {
            identifyEmail();
            return true;
        }
        return false;
    }

    @OnClick(R.id.submit_button)
    void onSubmitButtonClicked() {
        identifyEmail();
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
        final EditText editText = textInputLayout.getEditText();
        assert editText != null;
        final String email = editText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            textInputLayout.setError(getString(R.string.error_field_required));
            editText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayout.setError(getString(R.string.error_invalid_email));
            editText.requestFocus();
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
                        startActivity(LoginActivity.createIntent(context, email));
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
