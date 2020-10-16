package com.airtel.xstreamfiber.Activity;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airtel.xstreamfiber.R;

public class WifiSignalQuality extends AppCompatActivity implements View.OnClickListener {

    private ImageView imageView;
    private TextView tvStrength;
    private ImageView imgBack;
    private TextView txtToolbarTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_signal_quality);
        txtToolbarTitle=(TextView)findViewById(R.id.txtToolbarTitle);
        txtToolbarTitle.setText("Wifi Signal Strength");

        imageView =(ImageView)findViewById(R.id.imageView);
        tvStrength =(TextView)findViewById(R.id.tvStrength);
        imgBack=(ImageView)findViewById(R.id.imgBack);
        imgBack.setOnClickListener(this);

        checkwifiStrength();
    }

    private void checkwifiStrength() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = wifiInfo.getRssi();

        if (level >= -50) {
            //Excellent signal
            imageView.setImageResource(R.drawable.excellent);
            tvStrength.setText("Excellent");

        } else if (level < -50 && level >= -60) {
            //Good signal
            imageView.setImageResource(R.drawable.good);
            tvStrength.setText("Good");


        } else if (level < -60 && level >= -70) {
            //Fair signal
            imageView.setImageResource(R.drawable.fair);
            tvStrength.setText("Fair");


        } else if (level < -70 ) {
            //Weak signal
            imageView.setImageResource(R.drawable.weak);
            tvStrength.setText("Weak");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBack: {
                onBackPressed();
                break;
            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        WifiSignalQuality.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }
}
