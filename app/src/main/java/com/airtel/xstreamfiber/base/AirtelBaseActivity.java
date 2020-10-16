package com.airtel.xstreamfiber.base;

import android.os.Bundle;

import com.airtel.xstreamfiber.di.AppDI;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

public class AirtelBaseActivity extends RxAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppDI.getActivityComponent(this).inject(this);
    }

}
