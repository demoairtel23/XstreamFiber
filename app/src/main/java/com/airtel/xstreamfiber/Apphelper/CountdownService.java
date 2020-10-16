package com.airtel.xstreamfiber.Apphelper;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//Service for starting timer in guest Wifi
public class CountdownService extends Service {

    public static String str_receiver = "com.airtel.xstreamfiber.Apphelper.receiver";

    private Handler mHandler = new Handler();
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    SharedPreferences mpref;
    SharedPreferences.Editor mEditor;
    private  Runnable runnable;
    Long Hours, Minutes, Seconds;
    Intent intent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        mpref = getApplicationContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        mEditor = mpref.edit();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        intent = new Intent(str_receiver);

        Long enter_time = mpref.getLong("enter_time",0);
        Long given_time = mpref.getLong("given_time",0);
        mpref.getInt("duration",0);

//        String stop_service = mpref.getString("stopService", "");
//        if (stop_service.equals("stop"))
//            stopSelf();

        countDownStart(given_time);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void countDownStart(final Long given) {

        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    mHandler.postDelayed(this, 1000);

                    String stop_service = mpref.getString("Service", "");
                    if (stop_service.equals("stop")) {

                        intent.putExtra("hours", 0);
                        intent.putExtra("minutes", 0);
                        intent.putExtra("seconds", 0);

                        mEditor.putBoolean("finish", true);

                        intent.putExtra("finish", true);

                        intent.putExtra("button", "visible");
                        sendBroadcast(intent);

                        mEditor.clear();
                        stopSelf();
                        //stopService(new Intent(CountdownService.str_receiver));
                        mHandler.removeCallbacks(runnable);
                       // stopSelf();

                    }

                    Date current_calendar = new Date();

                    boolean v = given >= current_calendar.getTime();
                    if (given >= current_calendar.getTime()) {

                        mEditor.putBoolean("finish", false);

                        long diff =  given/1000 - current_calendar.getTime()/1000;
                        Hours = diff / (60 * 60 ) % 24;
                        Minutes = diff / (60 ) % 60;
                        Seconds = diff % 60;


                        intent.putExtra("hours", Hours);
                        intent.putExtra("minutes", Minutes);
                        intent.putExtra("seconds", Seconds);

                        intent.putExtra("finish", false);



                        sendBroadcast(intent);


                    } else {


                        intent.putExtra("hours", 0);
                        intent.putExtra("minutes", 0);
                        intent.putExtra("seconds", 0);

                        mEditor.putBoolean("finish", true);

                        intent.putExtra("finish", true);

                        intent.putExtra("button", "visible");
                        sendBroadcast(intent);

                        mEditor.clear();

                        stopSelf();
                        //stopService(new Intent(CountdownService.str_receiver));
                        mHandler.removeCallbacks(runnable);

                    }
                } catch (Exception ignored) {
                }
            }
        };
        mHandler.postDelayed(runnable, 0);
    }

    @Override
    public boolean stopService(Intent name) {


        intent.putExtra("hours", 0);
        intent.putExtra("minutes", 0);
        intent.putExtra("seconds", 0);

        mEditor.putBoolean("finish", true);

        intent.putExtra("finish", true);

        intent.putExtra("button", "visible");
        sendBroadcast(intent);
        return super.stopService(name);
    }
}