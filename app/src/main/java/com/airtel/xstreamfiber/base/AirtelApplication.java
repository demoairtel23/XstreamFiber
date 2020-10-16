package com.airtel.xstreamfiber.base;

import androidx.multidex.MultiDexApplication;

import com.airtel.xstreamfiber.Util.FontsOverride;

public class AirtelApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/Roboto.ttf");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/Roboto.ttf");
        FontsOverride.setDefaultFont(this, "SERIF", "fonts/Roboto.ttf");
        FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/Roboto.ttf");
    }
}
