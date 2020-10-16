
package com.airtel.xstreamfiber.di.modules;

import android.app.Application;
import android.content.Context;

import com.airtel.xstreamfiber.network.NetworkClient;
import com.airtel.xstreamfiber.network.NetworkMonitor;
import com.airtel.xstreamfiber.usecase.base.AndroidUseCaseComposer;
import com.airtel.xstreamfiber.usecase.base.UseCaseComposer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class AppApplicationModule {

    private final Application application;

    public AppApplicationModule(final Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    NetworkClient provideNetworkClient(NetworkMonitor networkMonitor) {
        return NetworkClient.getInstance(networkMonitor);
    }
    @Provides
    UseCaseComposer provideUseCaseComposer() {
        return new AndroidUseCaseComposer();
    }


    @Provides
    Application provideApplication() {
        return application;
    }

    @Provides
    Context provideContext() {
        return application;
    }

    @Provides
    @Singleton
    NetworkMonitor provideNetworkMonitor() {
        return new NetworkMonitor(application);
    }

//    @Provides
//    @Singleton
//    public AccountsRepository providesAccoountsRepository(NetworkClient networkClient, ApplicationStorage applicationStorage) {
//        return new NBAccountsRepository(networkClient, applicationStorage);
//    }

}
