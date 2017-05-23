package com.cypherpunk.privacy;

import com.cypherpunk.privacy.dagger.ClientModule;
import com.cypherpunk.privacy.domain.repository.NetworkRepository;

import okhttp3.OkHttpClient;

/**
 * mock for test
 */
class MockClientModule extends ClientModule {

    private final NetworkRepository networkRepository;

    MockClientModule(NetworkRepository networkRepository) {
        this.networkRepository = networkRepository;
    }

    @Override
    public NetworkRepository provideCypherpunkService(OkHttpClient httpClient) {
        return networkRepository;
    }

    @Override
    public OkHttpClient provideHttpClient() {
        return null;
    }
}
