package com.airtel.xstreamfiber.Activity;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.airtel.xstreamfiber.Apphelper.CountdownService;
import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.Util.KeyboardUtils;
import com.airtel.xstreamfiber.Util.MCrypt;
import com.airtel.xstreamfiber.Util.MethodsUtil;
import com.airtel.xstreamfiber.Util.NetworkUtils;
import com.airtel.xstreamfiber.Util.UIUtils;
import com.airtel.xstreamfiber.model.SSIDModel;
import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

/*
    In guest wifi setting, as the activity open getSSID api is called to fetch guestSsid present already and displayed,
    if no guest ssid present then toast is shown "No Guest SSID available" and toggle is disabled. If user fills all the fields
    ssid, password and duration and click on submit button then, setGuestSSID API is called and on success timer starts through
    CountDownService and getSSID API is called to get the data which was set. If user switches the toggle on then Guest ssid is disabled
    by calling enable-disable guest ssid API and viceversa.
*/
public class GuestWifiSetting extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "GuestWifiSetting";
    private ImageView imgBack;
    private TextView txtToolbarTitle, tv_hours, tv_minutes, tv_seconds;
    private Button hr5, hr10, hr24, always, btnSubmitNetwork;
    private int selectedDuration = 100;
    private Runnable runnable;
    private Handler handler = new Handler();
    SharedPreferences mpref;
    SharedPreferences.Editor mEditor;
    Intent intent_service, intent;
    private Dialog mDialog;
    private String getSSIDsendUrl = "v1/ssid";
    private TextView txt2ghz, txt5ghz, tvAlways, tvDisabledToggle;
    private SwitchCompat switch_toggle;
    private String enableDisableGuestSsidEndUrl = "v1/ssid_enable";
    private EditText editSSID, editPass;
    //    private String setSsidPassEndUrl = "v1/create/guest_ssid";
    private String setSsidPassEndUrl = "v2/create/guest_ssid";
    boolean finishEdit, finishIntent;
    private TextView txt2GHzHeading, txt5GHzHeading, txtSingleHeading, txtSingleSSID, tvDisableButton;
    private String userSerial;
    private String apiKey;
    private LinearLayout llTimer;
    TextInputLayout userNameLayout;
    private boolean isKeyboardVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_wifi_setting);

        //Initializing all views
        txtToolbarTitle = (TextView) findViewById(R.id.txtToolbarTitle);

        editSSID = (EditText) findViewById(R.id.editSSID);
        editPass = (EditText) findViewById(R.id.editPass);

        tvAlways = findViewById(R.id.tvAlways);
        llTimer = findViewById(R.id.llTimer);

        hr5 = (Button) findViewById(R.id.hr5);
        hr10 = (Button) findViewById(R.id.hr10);
        hr24 = (Button) findViewById(R.id.hr24);
        always = (Button) findViewById(R.id.always);
        btnSubmitNetwork = (Button) findViewById(R.id.btnSubmitNetwork);

        tv_hours = (TextView) findViewById(R.id.tv_hours);
        tv_minutes = (TextView) findViewById(R.id.tv_minutes);
        tv_seconds = (TextView) findViewById(R.id.tv_seconds);

        txtToolbarTitle.setText("Guest WiFi Setting");
        imgBack = (ImageView) findViewById(R.id.imgBack);

        tvDisableButton = findViewById(R.id.tv_disable_button);
        tvDisabledToggle = findViewById(R.id.tvDisabledToggle);

        userNameLayout = findViewById(R.id.userNameLayout);

        txt2GHzHeading = (TextView) findViewById(R.id.txt2GHzHeading);
        txt5GHzHeading = (TextView) findViewById(R.id.txt5GHzHeading);
        txt2ghz = (TextView) findViewById(R.id.txt2ghz);
        txt5ghz = (TextView) findViewById(R.id.txt5ghz);
        txtSingleHeading = (TextView) findViewById(R.id.txtSingleHeading);
        txtSingleSSID = (TextView) findViewById(R.id.txtSingleSSID);

        imgBack.setOnClickListener(this);
        hr5.setOnClickListener(this);
        hr10.setOnClickListener(this);
        hr24.setOnClickListener(this);
        always.setOnClickListener(this);
        btnSubmitNetwork.setOnClickListener(this);


        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);
        apiKey = dduSharedPref.getString("apiKey", "");

//        userSerial = "T5D7S18320913971";
//        apiKey = "64df95454c3f4f0896647a2d01cbef15";

        intent_service = new Intent(getApplicationContext(), CountdownService.class);

        mpref = getSharedPreferences("MyPref", MODE_PRIVATE);
        mEditor = mpref.edit();
        int timeDuration = mpref.getInt("duration", 0);

        //Checking if service has finished or not, if finished then enabling button
        finishEdit = mpref.getBoolean("finish", true);
        if (!finishEdit) {
            btnSubmitNetwork.setEnabled(false);
            btnSubmitNetwork.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
            tvDisableButton.setVisibility(View.VISIBLE);
        } else {
            btnSubmitNetwork.setEnabled(true);
            btnSubmitNetwork.setBackground(getResources().getDrawable(R.drawable.button_round));
            tvDisableButton.setVisibility(GONE);
        }

        //In case of orientation change, managing UI
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) userNameLayout.getLayoutParams();
            mlp.topMargin = 0;
            userNameLayout.setLayoutParams(mlp);

        } else {
            // In portrait
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) userNameLayout.getLayoutParams();
            mlp.topMargin = 10;
            userNameLayout.setLayoutParams(mlp);
        }

        switch_toggle = (SwitchCompat) findViewById(R.id.switch_toggle);

        //Checking if internet connected or not
        if (NetworkUtils.isNetworkConnected(GuestWifiSetting.this)) {
            getGuestNetwork();
        } else {
            Toast.makeText(GuestWifiSetting.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
        }

        switch_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isNetworkConnected(GuestWifiSetting.this)) {
                    final SwitchCompat btn = (SwitchCompat) v;
                    final boolean switchChecked = btn.isChecked();
                    if (switchChecked) {
                        btn.setChecked(false);
                    } else {
                        btn.setChecked(true);
                    }
                    showCustomDialog(switchChecked);
                } else {
                    final SwitchCompat btn = (SwitchCompat) v;
                    btn.setChecked(false);
                    Toast.makeText(GuestWifiSetting.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }


            }
        });

        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {
                isKeyboardVisible = isVisible;
            }
        });
    }

    //Enable-disable guest wifi
    private void enableDisableGuestNetwork(int active_status) {

        JSONObject mainJObject = new JSONObject();
        try {
            mainJObject.put("serial", userSerial);
            mainJObject.put("active_status", active_status);
        } catch (JSONException ignored) {
        }
        final String mRequestBody = mainJObject.toString();

        try {
            mDialog = UIUtils.showProgressDialog(GuestWifiSetting.this);
            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + enableDisableGuestSsidEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            if (response != null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");

                                if (statusCode.equals("200")) {
                                    Toast.makeText(GuestWifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                                    btnSubmitNetwork.setEnabled(true);
                                    btnSubmitNetwork.setBackground(getResources().getDrawable(R.drawable.button_round));
                                    tvDisableButton.setVisibility(GONE);
                                    editSSID.setText("");
                                    editPass.setText("");
                                    tv_hours.setText("HH:");
                                    tv_minutes.setText("MM:");
                                    tv_seconds.setText("SS");

//                                  intent_service = new Intent(getApplicationContext(), CountdownService.class);
//                                  stopService(intent_service);
//                                //  unregisterReceiver(broadcastReceiver);
//
//                                  if (intent_service != null) { //if service already running in case of deactivating stopService to stop timer
//                                      stopService(intent_service);
//                                      unregisterReceiver(broadcastReceiver);
//                                  }
//                                  else
//                                  {
//                                      Log.e("intent_service", "null");
//                                  }

                                    intent = new Intent(CountdownService.str_receiver);
                                    intent.putExtra("finish", true);
                                    sendBroadcast(intent);

                                    mEditor.putBoolean("finish", true);
                                    mEditor.commit();

                                    selectedDuration = 100;
                                    AfterSubmitSSIDPass(); //Stopping timer by passing selectedDuration as 100

                                    startActivity(new Intent(GuestWifiSetting.this, MainMenuActivity.class));
                                    finish();
                                    GuestWifiSetting.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);

                                } else {//If fails then toggle should be as it was ON or OFF
                                    Toast.makeText(GuestWifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                                    if (active_status == 0)
                                        switch_toggle.setChecked(false);
                                    else if (active_status == 1)
                                        switch_toggle.setChecked(true);
                                }


                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            UIUtils.dismissDialog(mDialog);

                            if (active_status == 0)
                                switch_toggle.setChecked(false);
                            else if (active_status == 1)
                                switch_toggle.setChecked(true);

                            if (error.networkResponse!=null){
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(GuestWifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(GuestWifiSetting.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(GuestWifiSetting.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) userNameLayout.getLayoutParams();
            mlp.topMargin = 0;
            userNameLayout.setLayoutParams(mlp);

        } else {
            // In portrait
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) userNameLayout.getLayoutParams();
            mlp.topMargin = 10;
            userNameLayout.setLayoutParams(mlp);
        }
    }

    //When clicked on Register showing dialog to inform user to connect to home network for registering
    public void showCustomDialog(boolean status) {
        final Dialog dialog = new Dialog(GuestWifiSetting.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.txt_title_dialog);
        title.setVisibility(GONE);

        TextView msg = (TextView) dialog.findViewById(R.id.txt_msg_dialog);
        if (status) {
            msg.setText("Do you want to disable your guest WiFi ?");
            //  selectedDuration = 100;
        } else if (!status) {
            msg.setText("Do you want to enable your guest WiFi ?");
        }

        TextInputEditText otpText = dialog.findViewById(R.id.inputText);
        otpText.setVisibility(View.GONE);

        Button positiveDialogButton = (Button) dialog.findViewById(R.id.btn_yes);
        positiveDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (status) {
                    enableDisableGuestNetwork(0);
                    switch_toggle.setChecked(true);
                } else if (!status) {
                    enableDisableGuestNetwork(1);
                    switch_toggle.setChecked(false);
                }
            }
        });

        Button negativeDialogButton = (Button) dialog.findViewById(R.id.btn_no);
        negativeDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (status)
                    switch_toggle.setChecked(false);
                else if (!status)
                    switch_toggle.setChecked(true);
            }
        });
        dialog.show();
    }

    //Getting guest SSId as we enter GuestWifiSetting
    private void getGuestNetwork() {

        JSONObject mainJObject = new JSONObject();
        try {
            mainJObject.put("serial", userSerial);
        } catch (JSONException ignored) {
        }
        final String mRequestBody = mainJObject.toString();

        try {
            mDialog = UIUtils.showProgressDialog(GuestWifiSetting.this);
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + getSSIDsendUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            if (response != null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");

                                if (statusCode.equals("200")) {

                                    JSONArray jsonArray = response.optJSONArray("data");

                                    ArrayList<SSIDModel> allObjArrayList = new ArrayList<>();

                                    for (int i = 0; i < jsonArray.length(); i++) {

                                        JSONObject newjb = jsonArray.optJSONObject(i);
                                        String SSID = newjb.optString("SSID");
                                        String instance = newjb.optString("Instance");
                                        String active = newjb.optString("active");

                                        SSIDModel ssidModel = new SSIDModel();
                                        ssidModel.setSSID(SSID);
                                        ssidModel.setInstance(instance);
                                        ssidModel.setActive(active);
                                        allObjArrayList.add(ssidModel);
                                    }
                                    ArrayList<SSIDModel> filteredArrayList = new ArrayList<>();

                                    for (int i = 0; i < allObjArrayList.size(); i++) {
                                        SSIDModel ssidModel = allObjArrayList.get(i);

                                        String instance = ssidModel.getInstance();

                                        //Filtering the whole list for getting instance 2 and 6
                                        if (instance.equals("2") || instance.equals("6")) {
                                            filteredArrayList.add(ssidModel);
                                        }
                                    }

                                    if (filteredArrayList.size() == 0) {

                                        //If no data in 2 or 6 instance then toggle disabled and show no guest ssid found
                                        switch_toggle.setEnabled(false);
                                        tvDisabledToggle.setVisibility(GONE);
                                        Toast.makeText(GuestWifiSetting.this, "Guest WiFi not available", Toast.LENGTH_SHORT).show();

                                    } else if (filteredArrayList.size() == 1) {
                                        switch_toggle.setEnabled(true);
                                        tvDisabledToggle.setVisibility(GONE);

                                        if (filteredArrayList.get(0).getActive().equals("false")) {
                                            //If data in 2 or 6 instance and active status is false then toggle is ON
                                            switch_toggle.setChecked(true);
                                            Toast.makeText(GuestWifiSetting.this, "Guest WiFi is deactivated", Toast.LENGTH_SHORT).show();
                                        }

                                        SSIDModel ssidModel = filteredArrayList.get(0);
                                        if (ssidModel.getInstance().equals("2")) {
                                            txtSingleHeading.setVisibility(View.VISIBLE);
                                            txtSingleSSID.setVisibility(View.VISIBLE);
                                            txtSingleHeading.setText("2.4 GHz");
                                            txtSingleSSID.setText(ssidModel.getSSID());
                                        } else if (ssidModel.getInstance().equals("6")) {
                                            txtSingleHeading.setVisibility(View.VISIBLE);
                                            txtSingleSSID.setVisibility(View.VISIBLE);
                                            txtSingleHeading.setText("5 GHz");
                                            txtSingleSSID.setText(ssidModel.getSSID());
                                        }
                                    } else if (filteredArrayList.size() == 2) {
                                        switch_toggle.setEnabled(true);
                                        tvDisabledToggle.setVisibility(GONE);

                                        if (filteredArrayList.get(0).getActive().equals("false") || filteredArrayList.get(1).getActive().equals("false")) {
                                            //If data in 2 and 6 instance and active status is false then toggle is ON
                                            switch_toggle.setChecked(true);
                                            Toast.makeText(GuestWifiSetting.this, "Guest WiFi is disabled", Toast.LENGTH_SHORT).show();
                                        }
                                        for (int j = 0; j < filteredArrayList.size(); j++) {
                                            SSIDModel ssidModel = filteredArrayList.get(j);
                                            if (ssidModel.getInstance().equals("2")) {
                                                txt2GHzHeading.setVisibility(View.VISIBLE);
                                                txt2ghz.setText(ssidModel.getSSID());
                                                txt2ghz.setVisibility(View.VISIBLE);

                                            } else if (ssidModel.getInstance().equals("6")) {
                                                txt5GHzHeading.setVisibility(View.VISIBLE);
                                                txt5ghz.setText(ssidModel.getSSID());
                                                txt5ghz.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }
                                } else {
                                    //If status code is not 200
                                    switch_toggle.setEnabled(true);
                                    tvDisabledToggle.setVisibility(GONE);
                                    txt2ghz.setText("");
                                    txt5ghz.setText("");
                                    txtSingleSSID.setText("");
                                    Toast.makeText(GuestWifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            UIUtils.dismissDialog(mDialog);
                            if (error.networkResponse!=null){
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(GuestWifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(GuestWifiSetting.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(GuestWifiSetting.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBack: {
                if (isKeyboardVisible) {
                    // Check if no view has focus:
                    View view = this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                } else {
                    onBackPressed();
                }
                break;
            }
            //for changing the color of button when selected or clicked
            case R.id.hr5: {
                hr10.setBackgroundResource(R.color.light_gray_color);
                hr10.setTextColor(getResources().getColor(R.color.btn_text_color));
                hr24.setBackgroundResource(R.color.light_gray_color);
                hr24.setTextColor(getResources().getColor(R.color.btn_text_color));
                always.setBackgroundResource(R.color.light_gray_color);
                always.setTextColor(getResources().getColor(R.color.btn_text_color));
                hr5.setBackgroundResource(R.color.colorPrimary);
                hr5.setTextColor(getResources().getColor(R.color.white));
                selectedDuration = 5;
                break;
            }
            case R.id.hr10: {
                hr5.setBackgroundResource(R.color.light_gray_color);
                hr5.setTextColor(getResources().getColor(R.color.btn_text_color));
                hr24.setBackgroundResource(R.color.light_gray_color);
                hr24.setTextColor(getResources().getColor(R.color.btn_text_color));
                always.setBackgroundResource(R.color.light_gray_color);
                always.setTextColor(getResources().getColor(R.color.btn_text_color));
                hr10.setBackgroundResource(R.color.colorPrimary);
                hr10.setTextColor(getResources().getColor(R.color.white));
                selectedDuration = 10;
                break;
            }
            case R.id.hr24: {
                hr5.setBackgroundResource(R.color.light_gray_color);
                hr5.setTextColor(getResources().getColor(R.color.btn_text_color));
                hr10.setBackgroundResource(R.color.light_gray_color);
                hr10.setTextColor(getResources().getColor(R.color.btn_text_color));
                always.setBackgroundResource(R.color.light_gray_color);
                always.setTextColor(getResources().getColor(R.color.btn_text_color));
                hr24.setBackgroundResource(R.color.colorPrimary);
                hr24.setTextColor(getResources().getColor(R.color.white));
                selectedDuration = 24;
                break;
            }
            case R.id.always: {
                hr5.setBackgroundResource(R.color.light_gray_color);
                hr5.setTextColor(getResources().getColor(R.color.btn_text_color));
                hr10.setBackgroundResource(R.color.light_gray_color);
                hr10.setTextColor(getResources().getColor(R.color.btn_text_color));
                hr24.setBackgroundResource(R.color.light_gray_color);
                hr24.setTextColor(getResources().getColor(R.color.btn_text_color));
                always.setBackgroundResource(R.color.colorPrimary);
                always.setTextColor(getResources().getColor(R.color.white));
                selectedDuration = 0;
                break;
            }
            case R.id.btnSubmitNetwork: {
                //Checking internet connection and then setting the data on submit
                if (NetworkUtils.isNetworkConnected(GuestWifiSetting.this)) {
                    setSSIDandPassword();
                } else {
                    Toast.makeText(GuestWifiSetting.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //Checking if the service is already running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //For receiving the broadcast fired by service
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            finishIntent = intent.getBooleanExtra("finish", true);


            if (finishIntent) {//If service is finished then unregistering broadcast and enabling submit button

                unregisterReceiver(broadcastReceiver);
                mEditor.putString("Service", "stop").apply();

                // stopService(intent_service);
                btnSubmitNetwork.setEnabled(true);
                btnSubmitNetwork.setBackground(getResources().getDrawable(R.drawable.button_round));
                tvDisableButton.setVisibility(GONE);
                tv_hours.setText("HH:");
                tv_minutes.setText("MM:");
                tv_seconds.setText("SS");
                //  selectedDuration = 100;
            } else {

                btnSubmitNetwork.setEnabled(false);
                btnSubmitNetwork.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
                tvDisableButton.setVisibility(View.VISIBLE);

                try {
                    Long hours = intent.getLongExtra("hours", 0);
                    Long minutes = intent.getLongExtra("minutes", 0);
                    Long seconds = intent.getLongExtra("seconds", 0);


                    if (hours < 10) {
                        tv_hours.setText("0" + hours + ":");
                    } else {
                        tv_hours.setText(hours + ":");
                    }
                    if (minutes < 10) {
                        tv_minutes.setText("0" + minutes + ":");
                    } else {
                        tv_minutes.setText(minutes + ":");
                    }
                    if (seconds < 10) {
                        tv_seconds.setText("0" + seconds);
                    } else {
                        tv_seconds.setText("" + seconds);
                    }
                } catch (ClassCastException e) {

                    int hours = intent.getIntExtra("hours", 0);
                    int minutes = intent.getIntExtra("minutes", 00);
                    int seconds = intent.getIntExtra("seconds", 00);


                    if (hours < 10) {
                        tv_hours.setText("0" + hours + ":");
                    } else {
                        tv_hours.setText(hours + ":");
                    }
                    if (minutes < 10) {
                        tv_minutes.setText("0" + minutes + ":");
                    } else {
                        tv_minutes.setText(minutes + ":");
                    }
                    if (seconds < 10) {
                        tv_seconds.setText("0" + seconds);
                    } else {
                        tv_seconds.setText("" + seconds);
                    }
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //Registering broadcast receiver
        registerReceiver(broadcastReceiver, new IntentFilter(CountdownService.str_receiver));
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            //handling if any exception arises
            unregisterReceiver(broadcastReceiver);

        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GuestWifiSetting.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }

    protected void onStop() {
        super.onStop();
    }

    private void setSSIDandPassword() {
        if (!TextUtils.isEmpty(editSSID.getText().toString().trim()) && !TextUtils.isEmpty(editPass.getText().toString().trim())) {
            String enteredSSID = editSSID.getText().toString().trim();
            String enteredpass = editPass.getText().toString().trim();


            if (!(enteredpass.length() > 7 && enteredpass.length() < 21)) {
                Toast.makeText(this, "Password should be 8-20 characters.", Toast.LENGTH_SHORT).show();
                // editPass.setError("password contain 8 char");
            } else if (selectedDuration == 100) {
                Toast.makeText(this, "Please select time", Toast.LENGTH_SHORT).show();
            } else {

                JSONObject mainJObject = new JSONObject();

                MCrypt mcrypt = new MCrypt();
                /* Encrypt */
                String encryptedPass = null;
                try {
                    encryptedPass = MCrypt.bytesToHex(mcrypt.encrypt(enteredpass)); //https://github.com/serpro/Android-PHP-Encrypt-Decrypt

                } catch (Exception ignored) {
                }
                try {

                    mainJObject.put("serial", userSerial);
                    mainJObject.put("ssid", enteredSSID);
                    mainJObject.put("password", encryptedPass);
                    mainJObject.put("time", selectedDuration);
                } catch (JSONException ignored) {
                }
                final String mRequestBody = mainJObject.toString();

                try {
                    mDialog = UIUtils.showProgressDialog(GuestWifiSetting.this);
                    mDialog.setCancelable(true);
                    mDialog.setCanceledOnTouchOutside(false);

                    RequestQueue queue = Volley.newRequestQueue(this);
                    String url = BuildConfig.baseUrl + setSsidPassEndUrl;
                    JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    UIUtils.dismissDialog(mDialog);
                                    if (response != null) {

                                        String statusCode = response.optString("StatusCode");
                                        String msg = response.optString("Message");
                                        if (statusCode.equals("200")) {

                                            // Toast.makeText(GuestWifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                                            editSSID.setText("");
                                            editPass.setText("");
                                            AfterSubmitSSIDPass(); // If data is successfully set then start the timer
                                            // getGuestNetwork(); // If data is successfully set then getting the set data to show
                                            Toast.makeText(GuestWifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(GuestWifiSetting.this, MainMenuActivity.class));
                                            finish();
                                            GuestWifiSetting.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);

                                        } else {
                                            Toast.makeText(GuestWifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    UIUtils.dismissDialog(mDialog);
                                    if (error.networkResponse!=null){
                                        String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                        Toast.makeText(GuestWifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                                    }else {
                                        if (error instanceof TimeoutError) {
                                            Toast.makeText(GuestWifiSetting.this, R.string.timeout, Toast.LENGTH_SHORT).show();
                                        } else if (error instanceof NoConnectionError) {
                                            Toast.makeText(GuestWifiSetting.this, R.string.error_noconnection, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(GuestWifiSetting.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                    ) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("key", apiKey);
                            params.put("Content-Type", "application/json");

//                            String credentials = BuildConfig.basicAuthUsername + ":" + BuildConfig.basicAuthPass;
//                            String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
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
                            //Toast.makeText(WifiSetting.this, getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                        }
                    });
                    queue.add(getRequest);

                } catch (Exception e) { // for caught any exception during the excecution of the service
                    UIUtils.dismissDialog(mDialog);
                    Toast.makeText(GuestWifiSetting.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(GuestWifiSetting.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void AfterSubmitSSIDPass() {

        if (selectedDuration == 0) //Selected duration always
        {
            btnSubmitNetwork.setEnabled(false);
            btnSubmitNetwork.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
            tvDisableButton.setVisibility(View.VISIBLE);
            llTimer.setVisibility(GONE);
            tvAlways.setVisibility(View.VISIBLE);
        } else if (selectedDuration == 100) // No selected duration or deactivated ssid or timer over
        {
            btnSubmitNetwork.setEnabled(true);
            btnSubmitNetwork.setBackground(getResources().getDrawable(R.drawable.button_round));
            tvDisableButton.setVisibility(GONE);
            editSSID.setText("");
            editPass.setText("");
            tv_hours.setText("HH:");
            tv_minutes.setText("MM:");
            tv_seconds.setText("SS");

            if (isMyServiceRunning(CountdownService.class)) { //if service already running in case of deactivating stopService to stop timer
                stopService(intent_service);
                //unregisterReceiver(broadcastReceiver);
            }
        } else if (selectedDuration == 5 || selectedDuration == 10 || selectedDuration == 24) { //to start timer in case of selected duration 5, 10, or 24 hours

            //  intent_service = new Intent(getApplicationContext(), CountdownService.class);

            btnSubmitNetwork.setEnabled(false);
            btnSubmitNetwork.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
            tvDisableButton.setVisibility(View.VISIBLE);

            Calendar enter_time = Calendar.getInstance();

            Calendar given_time = Calendar.getInstance();

            given_time.set(Calendar.HOUR, enter_time.get(enter_time.HOUR) + selectedDuration);
            given_time.set(Calendar.MINUTE, enter_time.get(enter_time.MINUTE));
            given_time.set(Calendar.SECOND, enter_time.get(enter_time.SECOND));

            Date enter_calendar = enter_time.getTime();
            Date given_calendar = given_time.getTime();

            mpref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            mEditor = mpref.edit();

            mEditor.putInt("duration", selectedDuration);
            mEditor.putLong("enter_time", enter_calendar.getTime());
            mEditor.putLong("given_time", given_calendar.getTime());
            mEditor.commit();

            if (!isMyServiceRunning(CountdownService.class)) {//starting service to start trhe timer
                mEditor.putString("Service", "start").apply();
                startService(intent_service);

            }
        }
    }
}
