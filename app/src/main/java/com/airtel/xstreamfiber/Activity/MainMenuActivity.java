package com.airtel.xstreamfiber.Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airtel.xstreamfiber.Adapters.GridAdapter;
import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.Util.MethodsUtil;
import com.airtel.xstreamfiber.Util.NetworkUtils;
import com.airtel.xstreamfiber.Util.UIUtils;
import com.airtel.xstreamfiber.model.ConnectedDeviceData;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

//import com.skyfishjy.library.RippleBackground;
//import com.balysv.materialripple.MaterialRippleLayout;

/*
    In MainMenuActivity, wifi connectivity is checked, if connected to Wifi then ssid and signal strength of the wifi is fetched
    through android local function and if not connected to wifi then same is shown in value place. Below is the gridview for
    travelling to various other screens of the application, on clicking to any grid, user is redirected tro the specified screen.
    On clicking on 3 dots(more option) user has 2 options, first is logging out to come out of the application and second is FAQ
    for Frequently asked questions for user guide.
*/
public class MainMenuActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private final String TAG = "MainMenuActvity";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LocationManager locationManager;
    String provider;
    private ConstraintLayout conslayout;
    TextView tv_ssid, tv_band_freq, tvName;
    GridView gridView;
    GridAdapter adapter;
    String freqBand;
    Calendar currentTime, repeatTime;
    private boolean is5GSupported, doubleBackToExitPressedOnce;
    private ArrayList<ConnectedDeviceData> connectedDeviceDataArrayList;
    int[] img = {R.drawable.ui_wifi_optimization,
            R.drawable.ui_wifi_setting, R.drawable.ui_internet_speedtest,
            R.drawable.ui_network_map, R.drawable.ui_wifi_coverage_analyzer,
            R.drawable.ui_guest_wifi, /*R.drawable.devicemanger,
            R.drawable.parentalcontrol, */R.drawable.ui_quick_diagnostic, R.drawable.ui_reboot_router};
    String[] name = {"WiFi Optimization", "WiFi Setting", "Internet Speed Test", "Connected Devices",
            "WiFi Coverage Analyzer", "Guest WiFi", /*"Device Manager",
            "Parental Controls", */"Quick Diagnostic", "Reboot Router"};
    private String my_activity;

    private static final int LOCATION = 1;

    public int gridHeight;
    private String getDeviceInfoUrl = "v1/device_info", logoutEndUrl = "v1/logout", getConnectedDeviceEndUrl = "v1/connected_device", upgradeEndUrl = "v1/upgrade";
    private TextView txtChannel, txt2;
    private TextView txtWifiStrength;
    private int level;
    private TextView txtSSID;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    RippleBackground rippleBackground;
    String activeDevice;
    private Dialog mDialog;
    private String userSerial;
    private String apiKey, strength, username;
    private int width, height;
    int secondsDelayed = 1;
    Handler mHandler;
    private String message = "New Version of the App is available. Do you want to install it?";
    private String myApkVersion = "https://ltesd.telesonic.in:8443/bbapp/Airtel_Broadband_v1.apk";
    private String currentApkVersion;
    LinearLayout llBottomNavDataUsage, llBottomNavHelpNFeedback, llBottomNavRateUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        gridView = findViewById(R.id.gridView);

        llBottomNavDataUsage = findViewById(R.id.llBottomNavDataUsage);
        llBottomNavHelpNFeedback = findViewById(R.id.llBottomNavHelpNFeedback);
        llBottomNavRateUs = findViewById(R.id.llBottomNavRateUs);
        conslayout = findViewById(R.id.conslayout);
        tv_ssid = findViewById(R.id.tv_ssid);
        tv_band_freq = findViewById(R.id.tv_band_freq);
        txt2 = findViewById(R.id.txt2);
        txtChannel = (TextView) findViewById(R.id.txtChannel);
        txtWifiStrength = (TextView) findViewById(R.id.txtWifiStrength);
        txtSSID = (TextView) findViewById(R.id.txtSSID);

        llBottomNavDataUsage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isNetworkConnected(MainMenuActivity.this)) {
                    Intent intent = new Intent(MainMenuActivity.this, CheckUserData.class);
                    startActivity(intent);
                    MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
                } else {
                    Toast.makeText(MainMenuActivity.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });
        llBottomNavHelpNFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If internet connected hit the api
                if (NetworkUtils.isNetworkConnected(MainMenuActivity.this)) {
                    Intent intent = new Intent(MainMenuActivity.this, HelpAndFeedbackActivity.class);
                    startActivity(intent);
                    MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
                } else {
                    Toast.makeText(MainMenuActivity.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });
        llBottomNavRateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MethodsUtil.sendToPlayStore(MainMenuActivity.this);
                /*Intent intent = new Intent(MainMenuActivity.this, ReferFriend.class);
                startActivity(intent);
                MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);*/
            }
        });

        tvName = findViewById(R.id.tvName);

        my_activity = "MainMenuActivity";

        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);
        apiKey = dduSharedPref.getString("apiKey", "");
        username = dduSharedPref.getString("name", "");

        //   tvName.setText(username);

        mHandler = new Handler();
        startRepeatingTask();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        sharedPreferences = getSharedPreferences(userSerial, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        activeDevice = sharedPreferences.getString("totalActive", "");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        int heightPortrait = (int) (height / 3.2);
        int heightLandscape = (int) (height / 2);

        //  gridHeight = height - heightPortrait ;


        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightLandscape);
            conslayout.setLayoutParams(params);

        } else {
            // In portrait
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPortrait);
            conslayout.setLayoutParams(params);

//            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, gridHeight);
//            params1.addRule(RelativeLayout.BELOW, R.id.conslayout);
//            gridView.setLayoutParams(params1);
        }

        gridView.post(new Runnable() {
            @Override
            public void run() {
                gridHeight = gridView.getHeight();

                adapter = new GridAdapter(MainMenuActivity.this, name, img);
                gridView.setAdapter(adapter);
            }
        });
        tryToReadSSID(); // For checking location permission

        //Ripple effect animation
        rippleBackground = (RippleBackground) findViewById(R.id.content);
        rippleBackground.startRippleAnimation();

        if (NetworkUtils.isNetworkConnected(MainMenuActivity.this)) {
            getDeviceInfo();
        }

//        currentApkVersion = "https://ltesd.telesonic.in:8443/bbapp/Airtel_Broadband_v1.apk";
//        showCustomDialog();

        if (sharedPreferences.getLong("repeat_date", 0) != 0) {

            // Long current_date =
            if (Calendar.getInstance().get(Calendar.YEAR) >= sharedPreferences.getLong("repeat_year", 0)
                    && Calendar.getInstance().get(Calendar.DAY_OF_YEAR) > sharedPreferences.getLong("repeat_date", 0)) {

                if (NetworkUtils.isNetworkConnected(MainMenuActivity.this)) {

                    callUpgradeApi();

                } else {
                    Toast.makeText(MainMenuActivity.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }

            } else {

                username = sharedPreferences.getString("name", "");
                if (username.length() > 28)
                    tvName.setText(username.substring(0, 25) + "...");
                else
                    tvName.setText(username);
            }
        } else {

            if (NetworkUtils.isNetworkConnected(MainMenuActivity.this)) {

                callUpgradeApi();

            } else {
                Toast.makeText(MainMenuActivity.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Check Wifi strength and name after every 10 seconds
    Runnable timedTask = new Runnable() {

        @Override
        public void run() {
            checkwifiStrength();
            mHandler.postDelayed(timedTask, 10000);
        }
    };

    private void getDeviceInfo() {

        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial", userSerial);
            mainJObject.put("version", BuildConfig.VERSION_NAME);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + getDeviceInfoUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            if (response != null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                if (statusCode.equals("200")) {

                                    JSONObject jsonObject = response.optJSONObject("data");

                                    String Model = jsonObject.optString("Model");
                                    String FirmwareVersion = jsonObject.optString("FirmwareVersion");
                                    String CPHealth = jsonObject.optString("CPE Health");
                                    int UpTime = jsonObject.optInt("UpTime");

                                    String Channel = jsonObject.optString("Channel");
                                    String channel5G = jsonObject.optString("Channel_5G");

                                    // Toast.makeText(DeviceManager.this, msg, Toast.LENGTH_SHORT).show();
                                    // UIUtils.showCustomToast(MainMenuActivity.this, msg);

                                    int p1 = UpTime % 60;
                                    int p2 = UpTime / 60;
                                    int p3 = p2 % 60;

                                    p2 = p2 / 60;


//                                    txtModelNo.setText(Model);
//                                    txtFirmware.setText(FirmwareVersion);
//                                    txtCphealth.setText(CPHealth);
//                                    txtUpTime.setText(p2 + ":" + p3 + ":" + p1);
//                                    txtChannel2.setText("Channel_2.4 GHz: "+Channel);
//                                    txtChannel5.setText("Channel_5 GHz: "+channel5G);

                                    if (!channel5G.equals("") && !channel5G.isEmpty()) {
                                        if (is5GSupported) {
                                            if (!freqBand.equals("5"))
                                                UIUtils.showCustomToast(MainMenuActivity.this, "Please connect to 5 GHz WiFi for better speed");
                                            // Toast.makeText(MainMenuActivity.this, "Please connect to 5GHz WiFi for better speed", Toast.LENGTH_LONG).show();
                                        }
                                    }


                                    //30 May 2020
                                    String logout = jsonObject.optString("logout");
                                    if (!logout.equals("") && !logout.isEmpty()) {
                                        if (logout.equalsIgnoreCase("yes")) {
                                            logoutMethod();
                                        } else if (logout.equalsIgnoreCase("no")) {
                                            logoutPart2();
                                        }
                                    }

                                } else {
                                    // UIUtils.dismissDialog(mDialog);
                                    // UIUtils.showCustomToast(MainMenuActivity.this, msg);
                                    // Toast.makeText(DeviceManager.this, msg, Toast.LENGTH_SHORT).show();
                                }

                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // UIUtils.dismissDialog(mDialog);
                            // UIUtils.showCustomToast(MainMenuActivity.this, getResources().getString(R.string.error_generic));
                            // Toast.makeText(DeviceManager.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("key", apiKey);
                    params.put("Content-Type", "application/json");

                    String auth = MethodsUtil.getAuth();
                    params.put("Authorization", auth);

                    return params;
                }

            };
            //Increasing timeout period
            getRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {

                    return 30000;
                }

                @Override
                public int getCurrentRetryCount() {

                    return 0;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                    UIUtils.dismissDialog(mDialog);
                    throw error;
                }
            });
            queue.add(getRequest);

        } catch (Exception ignored) { // for caught any exception during the excecution of the service

            // Toast.makeText(DeviceManager.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            //UIUtils.showCustomToast(MainMenuActivity.this, "Something went wrong!");
        }
    }

    void startRepeatingTask() {
        timedTask.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(timedTask);
    }

    @Override
    protected void onRestart() {

        my_activity = "MainMenuActivity";

        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);
        apiKey = dduSharedPref.getString("apiKey", "");

        //When back from minimized screen checking locaton permission again and getting deviceInfo
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        gridView.post(new Runnable() {
            @Override
            public void run() {
                gridHeight = gridView.getHeight();

                adapter = new GridAdapter(MainMenuActivity.this, name, img);
                gridView.setAdapter(adapter);
            }
        });

        rippleBackground.startRippleAnimation();

        super.onRestart();
    }

    protected void onStart() {

        my_activity = "MainMenuActivity";

        super.onStart();
        //Assume you want to read the SSID when the activity is started
        tryToReadSSID();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result array is empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        tryToReadSSID();
                    }

                }
                break;
            }
        }
    }

    private void tryToReadSSID() {
        //If requested permission isn't Granted yet
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request permission from user
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
        } else {//Permission already granted
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                String ssid = wifiInfo.getSSID();//Here you can access your SSID
            }
        }
    }

    //Managing UI in case of orientation change
    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        int heightPortrait = (int) (height / 3.2);
        int heightLandscape = (int) (height / 2);

        // gridHeight = height - heightPortrait ;


        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightLandscape);
            conslayout.setLayoutParams(params);

        } else {
            // In portrait
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPortrait);
            conslayout.setLayoutParams(params);

//            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, gridHeight);
//            params1.addRule(RelativeLayout.BELOW, R.id.conslayout);
//            gridView.setLayoutParams(params1);
        }
    }

    //On click grid elements opening the specific screens
    public void clickMenu(int position) {

        if (position == 0) {
            Intent i = new Intent(MainMenuActivity.this, OptimizeWifiNetwork.class);
            startActivity(i);
            MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

        } else if (position == 1) {
            Intent i = new Intent(MainMenuActivity.this, WifiSetting.class);
            startActivity(i);
            MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

        } else if (position == 2) {
            Intent i = new Intent(MainMenuActivity.this, SpeedTest.class);
            startActivity(i);
            MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

        } else if (position == 3) {
            Intent i = new Intent(MainMenuActivity.this, NetworkMapActivity.class);
            startActivity(i);
            MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

        } else if (position == 4) {
            Intent i = new Intent(MainMenuActivity.this, HeatMapActivity.class);
            startActivity(i);
            MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

        } else if (position == 5) {
            Intent i = new Intent(MainMenuActivity.this, GuestWifiSetting.class);
            startActivity(i);
            MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

        }/* else if (position == 6) {
            Intent i = new Intent(MainMenuActivity.this, DeviceManager.class);
            startActivity(i);
            MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

        } else if (position == 7) {
            Intent i = new Intent(MainMenuActivity.this, ParentalControlActivity.class);
            startActivity(i);
            MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

        } */ else if (position == 6) {
            Intent i = new Intent(MainMenuActivity.this, QuickDiagnosticNew.class);
            startActivity(i);
            MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

        } else if (position == 7) {
            Intent i = new Intent(MainMenuActivity.this, RebootRouter.class);
            startActivity(i);
            MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
        }
    }

    @Override
    protected void onResume() {
        //temp to be removed in next release build
        //tempMethod();


        my_activity = "MainMenuActivity";

        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);
        apiKey = dduSharedPref.getString("apiKey", "");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        gridView.post(new Runnable() {
            @Override
            public void run() {
                gridHeight = gridView.getHeight();

                adapter = new GridAdapter(MainMenuActivity.this, name, img);
                gridView.setAdapter(adapter);
            }
        });

        rippleBackground.startRippleAnimation();

        if (Constant.FLAG_SPLASH) {
            Constant.FLAG_SPLASH = false;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    MethodsUtil.checkApkVersion(MainMenuActivity.this);
                }
            });
        }

        super.onResume();

        checkwifiStrength();
    }

    private void tempMethod() {
        SharedPreferences sharedPreferences = getSharedPreferences("temp", MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("tempdata", false)) {
            sharedPreferences.edit().putBoolean("tempdata", true).apply();
            SharedPreferences userPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
            userPref.edit().clear().apply();
            Intent intent = new Intent(MainMenuActivity.this, Login.class);
            startActivity(intent);
            finish();
            MainMenuActivity.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
        }
    }


    private void checkwifiStrength() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            level = wifiInfo.getRssi();
            String ssidWithQuotes = wifiInfo.getSSID();
            String ssid = ssidWithQuotes.substring(1, ssidWithQuotes.length() - 1);

            is5GSupported = wifiManager.is5GHzBandSupported();


            tv_ssid.setText(ssid);

            int freq = wifiInfo.getFrequency();


            if (freq > 2400 && freq < 2500) {
                txtSSID.setText("SSID: " + ssid + "@2.4GHz");
                freqBand = "2.4";
                tv_band_freq.setText("2.4GHz");
                tv_band_freq.setVisibility(View.VISIBLE);
            } else if (freq > 5000 && freq < 6000) {
                txtSSID.setText("SSID: " + ssid + "@5GHz");
                freqBand = "5";
                tv_band_freq.setText("5GHz");
                tv_band_freq.setVisibility(View.VISIBLE);
            } else {
                txtSSID.setText("SSID: " + ssid);
                tv_band_freq.setVisibility(View.GONE);
            }
            if (level == 0) {
                strength = "No WiFi";
            } else if (level >= -50) {
                //Excellent signal
                strength = "Excellent";

            } else if (level < -50 && level >= -60) {
                //Good signal
                strength = "Good";

            } else if (level < -60 && level >= -70) {
                //Fair signal
                strength = "Fair";

            } else if (level < -70) {
                //Weak signal
                strength = "Weak";
            }
            txtWifiStrength.setText("WiFi Strength: " + strength);
        } else {
            freqBand = "No WiFi";
            txtSSID.setText("SSID: No WiFi");
            strength = "WiFi not connected";
            tv_band_freq.setVisibility(View.GONE);
            tv_ssid.setText("WiFi not connected");
            txtWifiStrength.setText("WiFi Strength: WiFi not connected");
        }
        // handler.postDelayed(timedTask, 60000);
        //handler.post(timedTask);
        // timedTask.run();
    }

    public void showPopup(View view) {

        PopupMenu popup = new PopupMenu(MainMenuActivity.this, view);
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.registrationmenu, popup.getMenu());


        //item text color
        /*MenuItem rate_us = popup.getMenu().findItem(R.id.rate_us);
        SpannableString s = new SpannableString(rate_us.getTitle().toString());
        s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
        rate_us.setTitle(s);*/

        //item text color
        MenuItem signout = popup.getMenu().findItem(R.id.signout);
        SpannableString sSignout = new SpannableString(signout.getTitle().toString());
        sSignout.setSpan(new ForegroundColorSpan(Color.WHITE), 0, sSignout.length(), 0);
        signout.setTitle(sSignout);


        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        int id = item.getItemId();

        /*if (id == R.id.check_user_data) {

            Intent intent = new Intent(MainMenuActivity.this, CheckUserData.class);
            startActivity(intent);
            MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

            return true;
        } else if (id == R.id.refer_friend) {
            Intent intent = new Intent(MainMenuActivity.this, ReferFriend.class);
            startActivity(intent);
            MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

            return true;
        } else*/
        /*if (id == R.id.rate_us) {
            MethodsUtil.sendToPlayStore(MainMenuActivity.this);
            return true;
        } else*/
        if (id == R.id.signout) {

            SharedPreferences userPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
            userPref.edit().clear().apply();

            Intent intent = new Intent(MainMenuActivity.this, Login.class);
            startActivity(intent);
            finish();
            MainMenuActivity.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);

            return true;
        }/* else if (id == R.id.tool) {
            //If internet connected hit the api
            if (NetworkUtils.isNetworkConnected(MainMenuActivity.this)) {
                Intent intent = new Intent(MainMenuActivity.this, FAQActivity.class);
                startActivity(intent);
                MainMenuActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
            } else {
                Toast.makeText(MainMenuActivity.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
            }

            return true;
        } */ else
            return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
//            super.onBackPressed();
            MethodsUtil.minimizeApp(MainMenuActivity.this);
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    private void callUpgradeApi() {
        JSONObject mainJObject = new JSONObject();

        try {
            mainJObject.put("serial", userSerial);

        } catch (JSONException ignored) {
        }
        final String mRequestBody = mainJObject.toString();

        try {
//            mDialog = UIUtils.showProgressDialog(Login.this);
//            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + upgradeEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //UIUtils.dismissDialog(mDialog);
                            if (response != null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                if (statusCode.equals("200")) {
                                    JSONObject jsonObject = response.optJSONObject("data");
                                    if (jsonObject != null) {
                                        String username = jsonObject.optString("name");
                                        String upgrade = jsonObject.optString("upgrade");
                                        String m_button = jsonObject.optString("m_button");

                                        SharedPreferences loginPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
                                        loginPref.edit().putString("m_button", m_button).apply();


                                        sharedPreferences.edit().putString("name", username).apply();
                                        sharedPreferences.edit().putString("upgrade", upgrade).apply();

                                        try {
                                            JSONArray jsonArrLoadQues = jsonObject.getJSONArray(Constant.LOADING_QUES);
                                            String[] strArr = new String[jsonArrLoadQues.length()];
                                            for (int i = 0; i < jsonArrLoadQues.length(); i++) {
                                                strArr[i] = (String) jsonArrLoadQues.get(i);
                                            }
                                            MethodsUtil.saveArray(strArr,Constant.LOADING_QUES,MainMenuActivity.this);
//                                            LoadingQuestions.getInstance().setQuesArr(strArr);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }


//                                        currentApkVersion = "https://ltesd.telesonic.in:8443/bbapp/Airtel_Broadband_v1.apk";
//
//                                        if (!currentApkVersion.equals(myApkVersion))
//                                        {
//                                            showCustomDialog();
//                                        }


                                        gridView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                gridHeight = gridView.getHeight();

                                                adapter = new GridAdapter(MainMenuActivity.this, name, img);
                                                gridView.setAdapter(adapter);
                                            }
                                        });

                                        currentTime = Calendar.getInstance();
                                        repeatTime = Calendar.getInstance();

                                        repeatTime.set(Calendar.DAY_OF_YEAR, currentTime.get(Calendar.DAY_OF_YEAR) + 1);
                                        repeatTime.set(Calendar.YEAR, currentTime.get(Calendar.YEAR));

                                        if (currentTime.get(Calendar.DAY_OF_YEAR) + 1 > 365) {
                                            repeatTime.set(Calendar.YEAR, currentTime.get(Calendar.YEAR) + 1);
                                        }

                                        long repeat_date = repeatTime.get(Calendar.DAY_OF_YEAR);
                                        long repeat_year = repeatTime.get(Calendar.YEAR);

                                        editor.putLong("repeat_date", repeat_date).apply();
                                        editor.putLong("repeat_year", repeat_year).apply();

                                        if (username.length() > 28)
                                            tvName.setText(username.substring(0, 25) + "...");
                                        else
                                            tvName.setText(username);
//                                        Intent intent = new Intent(Login.this, MainMenuActivity.class);
//                                        startActivity(intent);
//                                        finish();
//                                        Login.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

                                    }
                                    // Toast.makeText(Login.this, msg, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainMenuActivity.this, msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // UIUtils.dismissDialog(mDialog);
                            if (error.networkResponse != null) {
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(MainMenuActivity.this, msg, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainMenuActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    params.put("key", apiKey);

//                    String credentials = BuildConfig.basicAuthUsername + ":" + BuildConfig.basicAuthPass;
//                    String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    String auth = MethodsUtil.getAuth();
                    params.put("Authorization", auth);

                    return params;
                }

            };
            //Handling timeout, increasing the wait time
            getRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 30000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 0;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                    UIUtils.dismissDialog(mDialog);
                    throw error;
                }
            });
            queue.add(getRequest);

        } catch (Exception e) { // for caught any exception during the excecution of the service

            UIUtils.dismissDialog(mDialog);
            Toast.makeText(MainMenuActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }


    //When clicked on Register showing dialog to inform user to connect to home network for registering
    public void showCustomDialog() {
        final Dialog dialog = new Dialog(MainMenuActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        View view = dialog.findViewById(R.id.viewBwBtn);
        view.setVisibility(View.GONE);

        TextView title = (TextView) dialog.findViewById(R.id.txt_title_dialog);
        title.setText("Update");

        TextView msg = (TextView) dialog.findViewById(R.id.txt_msg_dialog);
        msg.setText(message);
        // msg.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);

        TextInputEditText otpText = dialog.findViewById(R.id.inputText);
        otpText.setVisibility(View.GONE);

        Button positiveDialogButton = (Button) dialog.findViewById(R.id.btn_yes);
        positiveDialogButton.setText("Update");
        positiveDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Uri intentUri = Uri.parse(currentApkVersion);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(intentUri);
                startActivity(intent);
            }
        });

        Button negativeDialogButton = (Button) dialog.findViewById(R.id.btn_no);
        negativeDialogButton.setVisibility(View.GONE);
        negativeDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void internetCheckApi() {

        try {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://www.google.com/";
            StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //UIUtils.dismissDialog(mDialog);
                            if (response != null) {
                                Toast.makeText(MainMenuActivity.this, "Response " + response.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // UIUtils.dismissDialog(mDialog);
                            if (error.networkResponse != null) {
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(MainMenuActivity.this, msg, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainMenuActivity.this, "message " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
            queue.add(getRequest);

        } catch (Exception e) { // for caught any exception during the excecution of the service

            UIUtils.dismissDialog(mDialog);
            Toast.makeText(MainMenuActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }


    private void logoutPart2() {
        if (MethodsUtil.isLoginSessionOver(getApplicationContext())) {
            logoutMethod();
        } else {
            if (NetworkUtils.isNetworkConnected(MainMenuActivity.this)) {
                if (MethodsUtil.isLoginCpeSessionOver(getApplicationContext())) {
                    callLogoutApi();
                }
            }
        }
    }

    private void callLogoutApi() {

        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial_no", userSerial);
            mainJObject.put("version", BuildConfig.VERSION_NAME);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + logoutEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (response != null) {
                                String logoutText = response.optString("logout");
                                int logout_time = response.optInt("logout_time");
                                if (logoutText.toLowerCase().trim().equalsIgnoreCase("yes")) {
                                    logoutMethod();
                                } else {
                                    if (logout_time > 0)
                                        MethodsUtil.setLoginCpeSessionTime(getApplicationContext(), logout_time);
                                }

                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("key", apiKey);
                    params.put("Content-Type", "application/json");

                    String auth = MethodsUtil.getAuth();
                    params.put("Authorization", auth);

                    return params;
                }

            };
            //Increasing timeout period
            getRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {

                    return 30000;
                }

                @Override
                public int getCurrentRetryCount() {

                    return 0;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                    UIUtils.dismissDialog(mDialog);
                    throw error;
                }
            });
            queue.add(getRequest);

        } catch (Exception ignored) { // for caught any exception during the excecution of the service

            // Toast.makeText(DeviceManager.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            //UIUtils.showCustomToast(MainMenuActivity.this, "Something went wrong!");
        }
    }

    private void logoutMethod() {
        SharedPreferences userPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
        userPref.edit().clear().apply();

        Intent intent = new Intent(MainMenuActivity.this, Login.class);
        startActivity(intent);
        finish();
        MainMenuActivity.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);

        Toast.makeText(MainMenuActivity.this, "Logging out due to technical reason.", Toast.LENGTH_LONG).show();
    }
}