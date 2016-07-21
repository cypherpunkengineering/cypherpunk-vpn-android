package com.cypherpunk.android.vpn.data.api;

import android.support.annotation.NonNull;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;


public class CypherpunkClient {

    private CypherpunkService cypherpunkService;

    @NonNull
    public synchronized CypherpunkService getApi() {
        if (cypherpunkService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .client(createApiClient())
                    .baseUrl(CypherpunkService.ENDPOINT)
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build();
            cypherpunkService = retrofit.create(CypherpunkService.class);
            return cypherpunkService;
        }
        return cypherpunkService;
    }

    private OkHttpClient createApiClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.cookieJar(new CookieManager());
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(logging);
        return builder.build();
    }

}
