
package com.airtel.xstreamfiber.di.components;


import com.airtel.xstreamfiber.di.modules.AppFragmentModule;
import com.airtel.xstreamfiber.di.scopes.ActivityScope;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = { AppFragmentModule.class})
public interface AppFragmentComponent {
}
