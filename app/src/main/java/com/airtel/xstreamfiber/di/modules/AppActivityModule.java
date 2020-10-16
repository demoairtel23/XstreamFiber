
package com.airtel.xstreamfiber.di.modules;

import android.app.Activity;

import com.airtel.xstreamfiber.di.scopes.ActivityScope;
import com.airtel.xstreamfiber.network.NetworkClient;
import com.airtel.xstreamfiber.repository.NetworkMapRepository;

import dagger.Module;
import dagger.Provides;


@Module
public class AppActivityModule {

    private final Activity activity;

    public AppActivityModule(final Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityScope
    public NetworkMapRepository provideNetworkMapRepository(NetworkClient networkClient) {
        return new NetworkMapRepository(networkClient);
    }


}
