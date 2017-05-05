package com.cypherpunk.privacy;

import com.cypherpunk.privacy.dagger.ClientModule;
import com.cypherpunk.privacy.data.api.CypherpunkService;

import retrofit2.Retrofit;

/**
 * mock for test
 */
class MockClientModule extends ClientModule {

    private final CypherpunkService cypherpunkService;

    MockClientModule(CypherpunkService cypherpunkService) {
        this.cypherpunkService = cypherpunkService;
    }

    @Override
    public CypherpunkService provideCypherpunkService(Retrofit retrofit) {
        return cypherpunkService;
    }
}
