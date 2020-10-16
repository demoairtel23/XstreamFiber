

package com.airtel.xstreamfiber.di.modules;


import android.app.Service;

import dagger.Module;


@Module
public class AppServiceModule {

    private final Service service;

    public AppServiceModule(final Service service) {
        this.service = service;
    }

}
