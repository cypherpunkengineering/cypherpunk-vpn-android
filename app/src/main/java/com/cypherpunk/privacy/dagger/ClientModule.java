package com.cypherpunk.privacy.dagger;

import com.cypherpunk.privacy.data.api.CookieManager;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.JsonipService;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;


@Module
public class ClientModule {
    @Provides
    public OkHttpClient provideHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.cookieJar(new CookieManager());
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        //builder.addInterceptor(logging);
        return builder.build();
    }

    @Provides
    public Retrofit provideRetrofit(OkHttpClient httpClient) {
        return new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(CypherpunkService.ENDPOINT)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public CypherpunkService provideCypherpunkService(Retrofit retrofit) {
        return retrofit.create(CypherpunkService.class);
    }

    @Provides
    @Singleton
    public JsonipService provideJsonipService() {
        //TODO:
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.readTimeout(3, TimeUnit.SECONDS);
        //builder.addInterceptor(logging);

        Retrofit build = new Retrofit.Builder().client(builder.build())
                .baseUrl(JsonipService.ENDPOINT)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
        return build.create(JsonipService.class);
    }
}
