
package com.airtel.xstreamfiber.di.components;


import com.airtel.xstreamfiber.Activity.NetworkMapActivity;
import com.airtel.xstreamfiber.Activity.Splash;
import com.airtel.xstreamfiber.base.AirtelBaseActivity;
import com.airtel.xstreamfiber.di.modules.AppActivityModule;
import com.airtel.xstreamfiber.di.scopes.ActivityScope;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = {AppActivityModule.class, AppActivityModule.class})
public interface AppActivityComponent {
    void inject(Splash activity);
    void inject(AirtelBaseActivity activity);
    void inject(NetworkMapActivity activity);


}
