package com.cypherpunk.privacy.dagger;

import com.cypherpunk.privacy.BuildConfig;
import com.cypherpunk.privacy.data.api.CookieInterceptor;
import com.cypherpunk.privacy.data.api.CypherpunkService;
import com.cypherpunk.privacy.data.api.UserAgentInterceptor;

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
        builder.interceptors().add(new CookieInterceptor());
        builder.interceptors().add(new UserAgentInterceptor("CypherpunkPrivacy/Android/" + BuildConfig.VERSION_NAME));
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(logging);
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
}
