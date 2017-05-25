package com.cypherpunk.privacy.domain.repository.retrofit;

import android.support.annotation.NonNull;

import com.cypherpunk.privacy.domain.model.account.Account;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.adapter.AccountTypeAdapter;
import com.cypherpunk.privacy.domain.repository.retrofit.adapter.ExpirationAdapter;
import com.cypherpunk.privacy.domain.repository.retrofit.adapter.RenewalAdapter;
import com.cypherpunk.privacy.domain.repository.retrofit.requst.ChangeEmailRequest;
import com.cypherpunk.privacy.domain.repository.retrofit.requst.ChangePasswordRequest;
import com.cypherpunk.privacy.domain.repository.retrofit.requst.EmailRequest;
import com.cypherpunk.privacy.domain.repository.retrofit.requst.LoginRequest;
import com.cypherpunk.privacy.domain.repository.retrofit.requst.SignUpRequest;
import com.cypherpunk.privacy.domain.repository.retrofit.requst.UpgradeAccountRequest;
import com.cypherpunk.privacy.domain.repository.retrofit.result.RegionResult;
import com.cypherpunk.privacy.domain.repository.retrofit.result.StatusResult;
import com.squareup.moshi.Moshi;

import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * retrofit implementation of NetworkRepository
 */
public class RetrofitRepository implements NetworkRepository {

    @NonNull
    private final CypherpunkService service;

    public RetrofitRepository(@NonNull OkHttpClient httpClient) {
        this.service = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(CypherpunkService.ENDPOINT)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(new Moshi.Builder()
                        .add(new RenewalAdapter())
                        .add(new ExpirationAdapter())
                        .add(new AccountTypeAdapter())
                        .build()))
                .build()
                .create(CypherpunkService.class);
    }

    @NonNull
    @Override
    public Completable identifyEmail(@NonNull String email) {
        return service.identifyEmail(new EmailRequest(email));
    }

    @NonNull
    @Override
    public Single<StatusResult> signUp(@NonNull String email, @NonNull String password) {
        return service.signUp(new SignUpRequest(email, password));
    }

    @NonNull
    @Override
    public Completable resendEmail(@NonNull String email) {
        return service.resendEmail(new EmailRequest(email));
    }

    @NonNull
    @Override
    public Single<StatusResult> login(@NonNull String email, @NonNull String password) {
        return service.login(new LoginRequest(email, password));
    }

    @NonNull
    @Override
    public Completable recoverPassword(@NonNull String email) {
        return service.recoverPassword(new EmailRequest(email));
    }

    @NonNull
    @Override
    public Completable changeEmail(@NonNull String email, @NonNull String password) {
        return service.changeEmail(new ChangeEmailRequest(email, password));
    }

    @NonNull
    @Override
    public Completable changePassword(@NonNull String oldPassword, @NonNull String newPassword) {
        return service.changePassword(new ChangePasswordRequest(oldPassword, newPassword));
    }

    @NonNull
    @Override
    public Single<StatusResult> getAccountStatus() {
        return service.getAccountStatus();
    }

    @NonNull
    @Override
    public Single<StatusResult> upgradeAccount(String accountId, @NonNull String sku, String purchaseJson) {
        return service.upgradeAccount(new UpgradeAccountRequest(accountId, sku, purchaseJson));
    }

    @NonNull
    @Override
    public Single<Map<String, RegionResult>> serverList(@NonNull Account.Type type) {
        return service.serverList(type.value());
    }
}
