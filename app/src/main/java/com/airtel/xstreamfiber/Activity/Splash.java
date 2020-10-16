package com.airtel.xstreamfiber.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.Util.MethodsUtil;
import com.airtel.xstreamfiber.base.AirtelBaseActivity;

/*
    In splash, userSerial is checked if empty then redirected to login screen and if some value then redirected directly to MainMenu.
 */
public class Splash extends AirtelBaseActivity {

    private String userSerial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        Constant.FLAG_SPLASH = true;

        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);

        int secondsDelayed = 1;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                initActivity();
            }
        }, secondsDelayed * 1000);
    }



    private void initActivity() {
        if (userSerial != null && !userSerial.equals(""))//If user already logged in then userSerial is not empty, open MainMenu screen
        {
            startActivity(new Intent(Splash.this, MainMenuActivity.class));
            Splash.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
            finish();
        } else {
            //If user not logged in then userSerial is empty, open Login screen
            startActivity(new Intent(Splash.this, Login.class));
            Splash.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
            finish();
        }
    }

}
