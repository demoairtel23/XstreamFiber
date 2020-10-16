
package com.airtel.xstreamfiber.di.components;


import com.airtel.xstreamfiber.di.modules.AppActivityModule;
import com.airtel.xstreamfiber.di.modules.AppApplicationModule;
import com.airtel.xstreamfiber.di.modules.AppServiceModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppApplicationModule.class})
public interface ApplicationComponent {
    AppActivityComponent plus(AppActivityModule appActivityModule);

//    AppFragmentComponent plus(AppFragmentModule appfragmentModule);

    AppServiceComponent plus(AppServiceModule appServiceModule);
}
