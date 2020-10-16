
package com.airtel.xstreamfiber.di;

import android.app.Application;
import android.app.Service;

import com.airtel.xstreamfiber.base.AirtelBaseActivity;
import com.airtel.xstreamfiber.di.components.AppActivityComponent;
import com.airtel.xstreamfiber.di.components.AppServiceComponent;
import com.airtel.xstreamfiber.di.components.ApplicationComponent;
import com.airtel.xstreamfiber.di.components.DaggerApplicationComponent;
import com.airtel.xstreamfiber.di.modules.AppActivityModule;
import com.airtel.xstreamfiber.di.modules.AppApplicationModule;
import com.airtel.xstreamfiber.di.modules.AppServiceModule;

public class AppDI {

    private static ApplicationComponent applicationComponent;

    public static ApplicationComponent getApplicationComponent(AirtelBaseActivity activity) {
        if (applicationComponent == null) {
            applicationComponent = DaggerApplicationComponent.builder()
                    .appApplicationModule(new AppApplicationModule(activity.getApplication()))
                    .build();
        }
        return applicationComponent;
    }


    public static AppActivityComponent getActivityComponent(AirtelBaseActivity activity) {
        return getApplicationComponent(activity).plus(new AppActivityModule(activity));
    }

    public static ApplicationComponent getApplicationComponent(Application application) {
        if (applicationComponent == null) {
            applicationComponent = DaggerApplicationComponent.builder()
                    .appApplicationModule(new AppApplicationModule(application))
                    .build();
        }
        return applicationComponent;
    }

    public static AppServiceComponent getServiceComponent(Service service) {
        return getApplicationComponent(service.getApplication()).plus(new AppServiceModule(service));
    }
}
