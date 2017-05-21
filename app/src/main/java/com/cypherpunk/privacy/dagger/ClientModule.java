package com.cypherpunk.privacy.dagger;

import com.cypherpunk.privacy.BuildConfig;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.RetrofitRepository;
import com.cypherpunk.privacy.domain.repository.retrofit.interceptor.CookieInterceptor;
import com.cypherpunk.privacy.domain.repository.retrofit.interceptor.UserAgentInterceptor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module
public class ClientModule {

    @Provides
    public OkHttpClient provideHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new CookieInterceptor())
                .addInterceptor(new UserAgentInterceptor("CypherpunkPrivacy/Android/" + BuildConfig.VERSION_NAME))
                .addInterceptor(new HttpLoggingInterceptor())
                .build();
    }

    @Provides
    @Singleton
    public NetworkRepository provideCypherpunkService(OkHttpClient httpClient) {
        return new RetrofitRepository(httpClient);
    }
}
