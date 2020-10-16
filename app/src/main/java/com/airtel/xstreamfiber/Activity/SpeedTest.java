package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.NetworkUtils;

/*
    In SpeedTest, it is checked if the given Application is installed in the device or not, if installed then the user is redirected
    to the app and if not installed redirected to Playstore of installing the app.
 */
public class SpeedTest extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "SpeedTest";
    private ImageView imgBack;
    private TextView txtToolbarTitle;
    private Button btnSpeedTest;
    private WebView webView;
    private LinearLayout llSpeedTest;
    private Dialog mDialog;
    private String URL_SPEEDTEST = "https://www.speedtest.net/";
    private String my_url = "https://play.google.com/store/apps/details?id=org.zwanoo.android.speedtest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_speed_test);
        txtToolbarTitle=(TextView)findViewById(R.id.txtToolbarTitle);
        txtToolbarTitle.setText("Speed Test");
        imgBack=(ImageView)findViewById(R.id.imgBack);
        imgBack.setOnClickListener(this);

        btnSpeedTest = (Button) findViewById(R.id.btnSpeedTest);
        btnSpeedTest.setOnClickListener(this);

        webView = (WebView) findViewById(R.id.webView);
        llSpeedTest = (LinearLayout) findViewById(R.id.llSpeedTest);

        if (!NetworkUtils.isNetworkConnected(SpeedTest.this)) {
            Toast.makeText(SpeedTest.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBack: {
                onBackPressed();
                break;
            }
            case R.id.btnSpeedTest: {
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo[] networkInfo = new NetworkInfo[0];
                if (conManager != null) {
                    networkInfo = conManager.getAllNetworkInfo();
                }
                for (NetworkInfo netInfo : networkInfo) {
                    if (netInfo.getTypeName().equalsIgnoreCase("WIFI")) {
                        if (!netInfo.isConnected()) {
                            Toast.makeText(SpeedTest.this, "Please connect to Home WiFi to proceed.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }


                //new : added on 14 Sept 2020
                Intent intent = new Intent(SpeedTest.this, SpeedTestActivity.class);
                intent.putExtra("pageUrl", URL_SPEEDTEST);
                startActivity(intent);
                SpeedTest.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

                //old
                /*final String appPackageName = "org.zwanoo.android.speedtest";
                boolean isAppInstalled = appInstalledOrNot(appPackageName);
                if(isAppInstalled) {
                    //This intent will help you to launch if the package is already installed
                    Intent LaunchIntent = getPackageManager()
                            .getLaunchIntentForPackage(appPackageName);
                    startActivity(LaunchIntent);

                } else {
                    //Redirect to play store, if application not already installed
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }*/
            }
        }
    }

    //Checking if app is already installed
    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SpeedTest.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }
}
