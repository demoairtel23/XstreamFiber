package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.Util.MethodsUtil;
import com.airtel.xstreamfiber.Util.NetworkUtils;
import com.airtel.xstreamfiber.Util.UIUtils;
import com.airtel.xstreamfiber.base.AirtelBaseActivity;
import com.airtel.xstreamfiber.di.AppDI;
import com.airtel.xstreamfiber.model.LoadingQuestions;
import com.airtel.xstreamfiber.network.response.ConnectedDeviceResponse;
import com.airtel.xstreamfiber.network.response.ConnectedDeviceRowData;
import com.airtel.xstreamfiber.view.NetworkMapView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
    In Network Map (now as Connected Devices) Activity, as the user enters the screen an API is called for getting all the connected devices to the wifi
    whether  ethernet or wireless and in wireless whether 2.4GHz or 5GHz. And accordingly flow diagram is set if all 3 types
    are present or any 2 types are present or any of the 1 type is present.
*/
public class NetworkMapActivity extends AirtelBaseActivity implements View.OnClickListener, NetworkMapView {

    private final String TAG = "NetworkMapActivity";
    private LinearLayout llLeft, llRight, llParentEthernet, llSingle, llSingleWireless, ll5SingleWireless;
    private Context context;
    private View view, viewSingle;
    private TextView txtToolbarTitle, tv_single, tvNoDevices;
    private ImageView imgBack;
    //    private ProgressBar progressBar;
    String wireless = "Wireless";
    String ethernet = "Ethernet";
    String frequencyBand24ghz = "2.4GHz";
    String frequencyBand5ghz = "5GHz";
    String inactive = "Inactive";
    String active = "active";
    String key_signalStrength = "SignalStrength";
    String key_active = "Active";
    String key_hostname = "HostName";
    String key_interfaceType = "InterfaceType";
    String key_frequencyBand = "frequencyBand";
    String key_macAddress = "MACAddress";
    String signal_excellent = "Excellent";
    String signal_good = "Good";
    String signal_fair = "Fair";
    String signal_weak = "Weak";
    String not_available = "Not Available";
    String message;
    private Dialog mDialog;
    private String networkMapEndUrl = "v1/connected_device";
    List<ConnectedDeviceRowData> activeWireless24GhList;
    List<ConnectedDeviceRowData> activeWireless5GhList;
    List<ConnectedDeviceRowData> activeEthernetList;
    LayoutInflater layoutInflater;
    Calendar currentTime, repeatTime;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String userSerial, apiKey;
    Bundle bundle;
    Handler mHandler;
    View ethernetView, wireLessView, g5view, g24view, verticalView2, verticalView4, verticalView54;
    TextView tvWireLess, tvg24, tvG5, tvEtherNet, tvSingleWireless, tv5SingleWireless;
    int repeat_after_min = 2;

    List<String> loading_ques;
//    @Inject
//    NetworkMapPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_network_map);
        AppDI.getActivityComponent(this).inject(this);
        txtToolbarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
//        txtToolbarTitle.setText("Network Map");
        txtToolbarTitle.setText("Connected Devices");

        imgBack = (ImageView) findViewById(R.id.imgBack);
        imgBack.setOnClickListener(this);

        tvNoDevices = findViewById(R.id.tvNoDevices);

        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);
        apiKey = dduSharedPref.getString("apiKey", "");


        layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;

        sharedPreferences = getSharedPreferences(userSerial, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        initViews();

        context = getApplicationContext();
        if (sharedPreferences.getLong("repeat_time", 0) != 0) {

            if (Calendar.getInstance().getTime().getTime() > (sharedPreferences.getLong("repeat_time", 0))) {
                if (NetworkUtils.isNetworkConnected(NetworkMapActivity.this)) {
                    callNetworkMapApi();
                } else {
                    Toast.makeText(NetworkMapActivity.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(NetworkMapActivity.this, getString(R.string.refresh_after_two_minutes), Toast.LENGTH_LONG).show(); //refresh_after_two_minutes
                setData();
            }
        } else {
            if (NetworkUtils.isNetworkConnected(NetworkMapActivity.this)) {
                callNetworkMapApi();
            } else {
                Toast.makeText(NetworkMapActivity.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void initViews() {
        llLeft = findViewById(R.id.ll_left);
        llRight = findViewById(R.id.ll_right);
        llParentEthernet = findViewById(R.id.parent_ethernet);
//        progressBar = findViewById(R.id.contentLoading);

        ethernetView = findViewById(R.id.view7);
        wireLessView = findViewById(R.id.view3);
        g5view = findViewById(R.id.view8);
        g24view = findViewById(R.id.view5);

        tvWireLess = (TextView) findViewById(R.id.tv_wireless);
        tvEtherNet = (TextView) findViewById(R.id.tv_ethernet);
        tvG5 = (TextView) findViewById(R.id.tv_5g);
        tvg24 = (TextView) findViewById(R.id.tv_2_5g);

        verticalView2 = findViewById(R.id.view2);
        verticalView4 = findViewById(R.id.view4);

        llSingle = findViewById(R.id.ll_single);
        //viewSingle = findViewById(R.id.view11);
        tv_single = findViewById(R.id.tv_band);

        llSingleWireless = findViewById(R.id.ll_single_wireless);
        tvSingleWireless = findViewById(R.id.tv_band_wireless);

        ll5SingleWireless = findViewById(R.id.ll_5single_wireless);
        tv5SingleWireless = findViewById(R.id.tv_5band_wireless);
        verticalView54 = findViewById(R.id.view54);

        showVisibility(false, ethernetView, wireLessView, g5view, g24view, tvWireLess, tvEtherNet, tvG5, tvg24, verticalView2, verticalView4);
    }

    void showVisibility(boolean b, View... views) {
        for (View v : views) {
            if (b)
                v.setVisibility(View.VISIBLE);
            else
                v.setVisibility(View.GONE);
        }
    }

    public static com.airtel.xstreamfiber.Fragment.NetworkMap newInstance() {
        return new com.airtel.xstreamfiber.Fragment.NetworkMap();
    }

    public void setData() {
        String totalActive = sharedPreferences.getString("totalActive", "");
        if (!TextUtils.isEmpty(totalActive) && totalActive.equals("0")) {
            tvNoDevices.setVisibility(View.VISIBLE);
//            Toast.makeText(NetworkMapActivity.this, "No Connected Devices", Toast.LENGTH_SHORT).show();
        } else {
            tvNoDevices.setVisibility(View.GONE);
            Gson gson = new Gson();
            String activeWireless24Gh = sharedPreferences.getString("activeWireless24GhList", "");
            Type type1 = new TypeToken<List<ConnectedDeviceRowData>>() {
            }.getType();
            activeWireless24GhList = gson.fromJson(activeWireless24Gh, type1);

            String activeWireless5Gh = sharedPreferences.getString("activeWireless5GhList", "");
            Type type2 = new TypeToken<List<ConnectedDeviceRowData>>() {
            }.getType();
            activeWireless5GhList = gson.fromJson(activeWireless5Gh, type2);

            String activeEthernet = sharedPreferences.getString("activeEthernetList", "");
            Type type3 = new TypeToken<List<ConnectedDeviceRowData>>() {
            }.getType();
            activeEthernetList = gson.fromJson(activeEthernet, type3);

            try {
                //If only 2.4GHz frequency band wireless devices are connected
                if (!activeWireless24GhList.isEmpty() && activeWireless5GhList.isEmpty() && activeEthernetList.isEmpty()) {
                    showVisibility(true, verticalView2, tv_single, llSingle, tvWireLess);
                    tv_single.setText("2.4 GHz");
                    for (int i = 0; i < activeWireless24GhList.size(); i++) {
                        view = layoutInflater.inflate(R.layout.layout_single_child, null);
                        llSingle.addView(view);
                        TextView tvPc1 = view.findViewById(R.id.tv_pcSing1);
                        ImageView hostImg = view.findViewById(R.id.iv_green1);
                        tvPc1.setText(activeWireless24GhList.get(i).getHostName());
                        if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_excellent)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.excellent_host_new));
                        } else if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_good) ||
                                activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(not_available)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        } else if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_fair)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.fair_signal_new));
                        } else if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_weak)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.weak_signal));
                        } else {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        }
                    }
                }
                //If only 5GHz frequency band wireless devices are connected
                else if (activeWireless24GhList.isEmpty() && !activeWireless5GhList.isEmpty() && activeEthernetList.isEmpty()) {
                    showVisibility(true, verticalView2, tv_single, llSingle, tvWireLess);
                    tv_single.setText("5 GHz");
                    for (int i = 0; i < activeWireless5GhList.size(); i++) {
                        view = layoutInflater.inflate(R.layout.layout_single_child, null);
                        llSingle.addView(view);
                        TextView tvPc1 = view.findViewById(R.id.tv_pcSing1);
                        ImageView hostImg = view.findViewById(R.id.iv_green1);
                        tvPc1.setText(activeWireless5GhList.get(i).getHostName());
                        if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_excellent)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.excellent_host_new));
                        } else if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_good) ||
                                activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(not_available)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        } else if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_fair)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.fair_signal_new));
                        } else if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_weak)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.weak_signal));
                        } else {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        }
                    }
                }
                //If only ethernet devices are connected
                else if (activeWireless24GhList.isEmpty() && activeWireless5GhList.isEmpty() && !activeEthernetList.isEmpty()) {
                    showVisibility(true, verticalView2, tv_single, llSingle, tvEtherNet);
                    for (int i = 0; i < activeEthernetList.size(); i++) {
                        view = layoutInflater.inflate(R.layout.layout_single_child, null);
                        llSingle.addView(view);
                        TextView tvPc1 = view.findViewById(R.id.tv_pcSing1);
                        ImageView hostImg = view.findViewById(R.id.iv_green1);
                        hostImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_green_screen_new));
                        tvPc1.setText(activeEthernetList.get(i).getHostName());
                    }
                }
                //If only 2.4GHz frequency band wireless devices and ethernet devices are connected
                else if (!activeWireless24GhList.isEmpty() && activeWireless5GhList.isEmpty() && !activeEthernetList.isEmpty()) {

                    ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) wireLessView.getLayoutParams();
                    p.setMargins(30, 0, 0, 0);
                    // wireLessView.setLayoutParams(p);
                    wireLessView.requestLayout();

                    tvSingleWireless.setText("2.4 GHz");
                    //Parse and show 2.4GHz data here. Modify loop based upon API response
                    for (int i = 0; i < activeWireless24GhList.size(); i++) {
                        showVisibility(true, tvWireLess, wireLessView, tvSingleWireless, llSingleWireless, verticalView2, verticalView4);
                        view = layoutInflater.inflate(R.layout.layout_single_child, null);
                        llSingleWireless.addView(view);
                        TextView tvPc1 = view.findViewById(R.id.tv_pcSing1);
                        ImageView hostImg = view.findViewById(R.id.iv_green1);
                        tvPc1.setText(activeWireless24GhList.get(i).getHostName());
                        if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_excellent)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.excellent_host_new));
                        } else if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_good) ||
                                activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(not_available)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        } else if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_fair)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.fair_signal_new));
                        } else if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_weak)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.weak_signal));
                        } else {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        }
                    }

                    //Parse and show ehternet data here.  Modify loop based upon API response
                    for (int i = 0; i < activeEthernetList.size(); i++) {
                        showVisibility(true, tvEtherNet, ethernetView, verticalView2);
                        view = layoutInflater.inflate(R.layout.layout_ehternet_data, null);
                        llParentEthernet.addView(view);
                        TextView tvPc1 = view.findViewById(R.id.tv_pc1);
                        tvPc1.setText(activeEthernetList.get(i).getHostName());
                    }
                }
                //If only 5GHz frequency band wireless devices and ethernet devices are connected
                else if (activeWireless24GhList.isEmpty() && !activeWireless5GhList.isEmpty() && !activeEthernetList.isEmpty()) {

                    ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) wireLessView.getLayoutParams();
                    p.setMargins(30, 0, 0, 0);
                    // wireLessView.setLayoutParams(p);
                    wireLessView.requestLayout();
                    tvSingleWireless.setText("5 GHz");
                    //Parse and show 5GHz data here.  Modify loop based upon API response
                    for (int i = 0; i < activeWireless5GhList.size(); i++) {
                        showVisibility(true, tvWireLess, wireLessView, tvSingleWireless, llSingleWireless, verticalView2, verticalView4);
                        view = layoutInflater.inflate(R.layout.layout_single_child, null);
                        llSingleWireless.addView(view);
                        TextView tvPc1 = view.findViewById(R.id.tv_pcSing1);
                        ImageView hostImg = view.findViewById(R.id.iv_green1);
                        tvPc1.setText(activeWireless5GhList.get(i).getHostName());
                        if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_excellent)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.excellent_host_new));
                        } else if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_good) ||
                                activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(not_available)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        } else if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_fair)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.fair_signal_new));
                        } else if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_weak)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.weak_signal));
                        } else {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        }
                    }

                    //Parse and show ehternet data here.  Modify loop based upon API response
                    for (int i = 0; i < activeEthernetList.size(); i++) {
                        showVisibility(true, tvEtherNet, ethernetView, verticalView2);
                        view = layoutInflater.inflate(R.layout.layout_ehternet_data, null);
                        llParentEthernet.addView(view);
                        TextView tvPc1 = view.findViewById(R.id.tv_pc1);
                        tvPc1.setText(activeEthernetList.get(i).getHostName());
                    }
                }
                //If 2GHz and  5GHz frequency band wireless devices are connected
                else if (!activeWireless24GhList.isEmpty() && !activeWireless5GhList.isEmpty() && activeEthernetList.isEmpty()) {

                    ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) wireLessView.getLayoutParams();
                    p.setMargins(30, 0, 0, 0);
                    //  wireLessView.setLayoutParams(p);
                    wireLessView.requestLayout();

                    tvSingleWireless.setText("2.4 GHz");
                    tv5SingleWireless.setText("5 GHz");

                    //Parse and show 2.4GHz data here. Modify loop based upon API response
                    for (int i = 0; i < activeWireless24GhList.size(); i++) {
                        showVisibility(true, tvWireLess, wireLessView, tvSingleWireless, llSingleWireless, verticalView2, verticalView4);
                        view = layoutInflater.inflate(R.layout.layout_single_child, null);
                        llSingleWireless.addView(view);
                        TextView tvPc1 = view.findViewById(R.id.tv_pcSing1);
                        ImageView hostImg = view.findViewById(R.id.iv_green1);
                        tvPc1.setText(activeWireless24GhList.get(i).getHostName());
                        if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_excellent)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.excellent_host_new));
                        } else if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_good) ||
                                activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(not_available)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        } else if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_fair)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.fair_signal_new));
                        } else if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_weak)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.weak_signal));
                        } else {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        }
                    }

                    //Parse and show 5GHz data here.  Modify loop based upon API response
                    for (int i = 0; i < activeWireless5GhList.size(); i++) {
                        showVisibility(true, ethernetView, verticalView2, verticalView54, ll5SingleWireless, tv5SingleWireless);
                        // showVisibility(true, tvWireLess, wireLessView, tvSingleWireless, llSingleWireless, verticalView2, verticalView4);
                        view = layoutInflater.inflate(R.layout.layout_ehternet_data, null);
                        ll5SingleWireless.addView(view);
                        TextView tvPc1 = view.findViewById(R.id.tv_pc1);
                        ImageView hostImg = view.findViewById(R.id.iv_green1);
                        tvPc1.setText(activeWireless5GhList.get(i).getHostName());
                        if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_excellent)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.excellent_host_new));
                        } else if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_good) ||
                                activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(not_available)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        } else if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_fair)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.fair_signal_new));
                        } else if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_weak)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.weak_signal));
                        } else {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        }
                    }
                }
                //If both frequency band wireless devices and ethernet devices are connected
                else {

                    //Parse and show 2.4GHz data here. Modify loop based upon API response
                    for (int i = 0; i < activeWireless24GhList.size(); i++) {
                        showVisibility(true, tvWireLess, tvg24, wireLessView, g24view, verticalView2, verticalView4);
                        view = layoutInflater.inflate(R.layout.layout_wireless_left, null);
                        llLeft.addView(view);
                        TextView tvPc1 = view.findViewById(R.id.tv_pc1);
                        ImageView hostImg = view.findViewById(R.id.iv_green1);
                        tvPc1.setText(activeWireless24GhList.get(i).getHostName());
                        if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_excellent)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.excellent_host_new));
                        } else if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_good) ||
                                activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(not_available)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        } else if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_fair)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.fair_signal_new));
                        } else if (activeWireless24GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_weak)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.weak_signal));
                        } else {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        }
                    }

                    //Parse and show 5GHz data here.  Modify loop based upon API response
                    for (int i = 0; i < activeWireless5GhList.size(); i++) {
                        showVisibility(true, tvWireLess, tvG5, wireLessView, g5view, verticalView2, verticalView4);
                        view = layoutInflater.inflate(R.layout.layout_wireless_right, null);
                        llRight.addView(view);
                        TextView tvPc1 = view.findViewById(R.id.tv_pc1);
                        ImageView hostImg = view.findViewById(R.id.iv_green1);
                        tvPc1.setText(activeWireless5GhList.get(i).getHostName());
                        if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_excellent)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.excellent_host_new));
                        } else if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_good) ||
                                activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(not_available)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        } else if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_fair)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.fair_signal_new));
                        } else if (activeWireless5GhList.get(i).getSignalStrength().equalsIgnoreCase(signal_weak)) {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.weak_signal));
                        } else {
                            hostImg.setImageDrawable(getResources().getDrawable(R.drawable.good_signal_new));
                        }
                    }

                    //Parse and show ehternet data here.  Modify loop based upon API response
                    for (int i = 0; i < activeEthernetList.size(); i++) {
                        showVisibility(true, tvEtherNet, ethernetView, verticalView2);
                        view = layoutInflater.inflate(R.layout.layout_ehternet_data, null);
                        llParentEthernet.addView(view);
                        TextView tvPc1 = view.findViewById(R.id.tv_pc1);
                        tvPc1.setText(activeEthernetList.get(i).getHostName());
                    }
                    showProgress(false);
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NetworkMapActivity.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
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
    public void showProgress(Boolean b) {
        /*if (b)
            progressBar.setVisibility(View.VISIBLE);
        else
            progressBar.setVisibility(View.GONE);*/
    }

    @Override
    public void showError(String msg) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        showSnackBar(getString(R.string.error_message_something_went_wrong));
    }

    @Override
    public void handleNetworkMapData(ConnectedDeviceResponse connectedDeviceResponse) {

        //  renderDynamicViews(connectedDeviceResponse);
    }

    @Override
    public void close() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //presenter.unbind();
    }

    public void showSnackBar(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public void callNetworkMapApi() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serial", userSerial);
        } catch (Exception ignored) {
        }
        try {
//            progressBar.setVisibility(View.VISIBLE);
            mDialog = UIUtils.showProgressDialogWithQuestions(NetworkMapActivity.this);
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(false);
            handler = new Handler();
            loading_ques = MethodsUtil.loadArray(Constant.LOADING_QUES, NetworkMapActivity.this);
            if (loading_ques.size() == 0) {
                loading_ques = MethodsUtil.loadArray(Constant.LOADING_QUES, NetworkMapActivity.this);
            }
            Collections.shuffle(loading_ques);

//            if (LoadingQuestions.getInstance().getQuesArr()!=null)
//                Collections.shuffle(Arrays.asList(LoadingQuestions.getInstance().getQuesArr()));
            handler.post(r);

            RequestQueue queue = Volley.newRequestQueue(NetworkMapActivity.this);
            String url = BuildConfig.baseUrl + networkMapEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            cancelHandler();
//                            progressBar.setVisibility(View.GONE);

                            if (response != null) {


                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");

                                if (statusCode.equals("200")) {

                                    currentTime = Calendar.getInstance();
                                    repeatTime = Calendar.getInstance();

                                    repeatTime.set(Calendar.HOUR, currentTime.get(Calendar.HOUR));
                                    repeatTime.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE) + repeat_after_min);
                                    if (currentTime.get(Calendar.MINUTE) + repeat_after_min >= 60) {
                                        repeatTime.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE) + repeat_after_min - 60);
                                        repeatTime.set(Calendar.HOUR, currentTime.get(Calendar.HOUR) + 1);
                                    }
                                    long repeat_time = repeatTime.getTime().getTime();
                                    editor.putLong("repeat_time", repeat_time).apply();


                                    try {

                                        JSONObject jobject1 = response.optJSONObject("data");

                                        String totalActive = jobject1.optString("totalActive");


                                        editor.putString("totalActive", totalActive).apply();

                                        JSONObject jsonObjectActive = jobject1.optJSONObject("active");

                                        if (totalActive.equals("0"))
                                            tvNoDevices.setVisibility(View.VISIBLE);
                                        else
                                            tvNoDevices.setVisibility(View.GONE);


                                        if (jobject1 != null) {
                                            if (jsonObjectActive != null) {

                                                activeWireless24GhList = new ArrayList<ConnectedDeviceRowData>();
                                                activeWireless5GhList = new ArrayList<ConnectedDeviceRowData>();
                                                activeEthernetList = new ArrayList<ConnectedDeviceRowData>();

                                                Iterator<String> keyIterator = jsonObjectActive.keys();
                                                while (keyIterator.hasNext()) {

                                                    String key = keyIterator.next();
                                                    JSONObject activeJObject = jsonObjectActive.getJSONObject(key);
                                                    String signalStrength = activeJObject.optString("SignalStrength");
                                                    String active = activeJObject.optString("Active");
                                                    String hostName = activeJObject.optString("HostName");
                                                    String interfaceType = activeJObject.optString("InterfaceType");
                                                    String frequencyBand = activeJObject.optString("frequencyBand");
                                                    String macAddress = activeJObject.optString("MACAddress");

                                                    ConnectedDeviceRowData deviceRowData = new ConnectedDeviceRowData();
                                                    deviceRowData.setActive(active);
                                                    deviceRowData.setSignalStrength(signalStrength);
                                                    deviceRowData.setInterfaceType(interfaceType);
                                                    deviceRowData.setFrequencyBand(frequencyBand);
                                                    deviceRowData.setMACAddress(macAddress);
                                                    deviceRowData.setHostName(hostName);

                                                    //Checking the response data and filling it in three arrays according to its interface type and frequency band
                                                    if (active.equalsIgnoreCase(deviceRowData.getActive())) {
                                                        if (wireless.equalsIgnoreCase(deviceRowData.getInterfaceType())) {
                                                            if (frequencyBand5ghz.equalsIgnoreCase(deviceRowData.getFrequencyBand())) {
                                                                activeWireless5GhList.add(deviceRowData);
                                                            } else
                                                                activeWireless24GhList.add(deviceRowData);
                                                        } else if (ethernet.equalsIgnoreCase(deviceRowData.getInterfaceType())) {
                                                            activeEthernetList.add(deviceRowData);
                                                        }
                                                    }
                                                }

                                                Gson gson = new Gson();
                                                String activeWireless24Gh = gson.toJson(activeWireless24GhList);
                                                editor.putString("activeWireless24GhList", activeWireless24Gh).apply();
                                                String activeWireless5Gh = gson.toJson(activeWireless5GhList);
                                                editor.putString("activeWireless5GhList", activeWireless5Gh).apply();
                                                String activeEthernet = gson.toJson(activeEthernetList);
                                                editor.putString("activeEthernetList", activeEthernet).apply();

                                                setData();
                                            } else {
//                                                progressBar.setVisibility(View.GONE);
                                                UIUtils.dismissDialog(mDialog);
                                                cancelHandler();
                                                if (totalActive.equals("0"))
                                                    Toast.makeText(NetworkMapActivity.this, "No Connected Devices", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    } catch (JSONException e) {
//                                        progressBar.setVisibility(View.GONE);
                                        UIUtils.dismissDialog(mDialog);
                                        cancelHandler();
                                        showSnackBar(getString(R.string.error_message_something_went_wrong));
                                    }
                                } else {
//                                    progressBar.setVisibility(View.GONE);
                                    UIUtils.dismissDialog(mDialog);
                                    cancelHandler();
                                    Toast.makeText(NetworkMapActivity.this, msg, Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            UIUtils.dismissDialog(mDialog);
                            cancelHandler();
//                            progressBar.setVisibility(View.GONE);
                            if (error.networkResponse != null) {
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(NetworkMapActivity.this, msg, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(NetworkMapActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("key", apiKey);
                    params.put("Content-Type", "application/json");
//                    String credentials = BuildConfig.basicAuthUsername + ":" + BuildConfig.basicAuthPass;
//                    String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    String auth = MethodsUtil.getAuth();
                    params.put("Authorization", auth);
                    return params;
                }
            };
            //Increasing timeout period
            getRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 35000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 0;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                    UIUtils.dismissDialog(mDialog);
                    cancelHandler();
                    throw error;
                }
            });
            queue.add(getRequest);

        } catch (Exception e) { // for caught any exception during the excecution of the service

            UIUtils.dismissDialog(mDialog);
            cancelHandler();
            Toast.makeText(NetworkMapActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelHandler() {
        if (handler != null) {
            handler.removeCallbacks(r);
        }
    }


    Handler handler;
    int count = 0;
    final Runnable r = new Runnable() {
        public void run() {
            if (loading_ques != null && loading_ques.size() > 0) {
                handler.postDelayed(this, 5000);
                runAnimation(mDialog, loading_ques.get(count));
                count++;
                if (count == loading_ques.size()) {
                    count = 0;
                    MethodsUtil.initLoadArray(Constant.LOADING_QUES,NetworkMapActivity.this);
                    loading_ques = MethodsUtil.loadArray(Constant.LOADING_QUES, NetworkMapActivity.this);
                }
            }
            /*if (LoadingQuestions.getInstance().getQuesArr()!=null && LoadingQuestions.getInstance().getQuesArr().length>0){
                handler.postDelayed(this, 5000);
                runAnimation(mDialog, LoadingQuestions.getInstance().getQuesArr()[count]);
                count++;
                if (count==LoadingQuestions.getInstance().getQuesArr().length)
                {
                    count=0;
                }
            }*/
        }
    };

    private void runAnimation(Dialog mDialog, String s) {
        Animation a = AnimationUtils.loadAnimation(this, R.anim.fade);
        a.reset();
        TextView tv = (TextView) mDialog.findViewById(R.id.tvLoadingQuestion);
        tv.setText(s);
        tv.clearAnimation();
        tv.startAnimation(a);
        if (!TextUtils.isEmpty(s)) {
            SharedPreferences prefs = getSharedPreferences(Constant.LOADING_QUES, 0);
            Map<String, ?> keys = prefs.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
//                Log.e("map values", entry.getKey() + ": " + entry.getValue().toString());
                if (s.equalsIgnoreCase(entry.getValue().toString())) {
                    MethodsUtil.saveUsedArray(entry.getKey(), NetworkMapActivity.this);
                }
            }
        }
    }
}
