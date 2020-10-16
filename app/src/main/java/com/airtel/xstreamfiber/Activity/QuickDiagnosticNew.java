package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.Util.MethodsUtil;
import com.airtel.xstreamfiber.Util.NetworkUtils;
import com.airtel.xstreamfiber.Util.UIUtils;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

/*
    In quick diagnostic, when the user clicks on Start Check then API is called and response time is checked if 5 sec has passed
    then 1st step is marked as completed, if 15 (5 + 10) sec has passed then 2nd step is marked as completed, if 35 (5 + 10 + 20) sec
    has passed then 3rd step is marked as completed. Other error cases are also handled accordingly.
*/
public class QuickDiagnosticNew extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgBack, imgCheckConnectivity, all_done;
    private TextView txtToolbarTitle, rebootText, progressTv;
    private TextView edit1, edit2, edit3, edit4;
    private TextView step1Name, step2Name, step3Name, step4Name;
    private ProgressBar loader1, loader2, loader3, loader4;
    private ImageView step1, step2, step3, step4;
    private ScrollView scrollDiagnostic;
    private String reebootRouterEndUrl = "v1/reboot";
    private String QuickDiagnosticEndUrl = "v1/quick_diagnose_new";
    private String getoptimizeNetworkResetUrl="v1/change_channel";
    private String raiseSRUrl = "v1/raiseSR";
    private Dialog mDialog;
    private String userSerial, apiKey;
    private static StringBuilder hexString;
    private Boolean isSuccessful = false;
    private String step = "step";
    private Button btRaiseSR;
    private Button btOptimizeWifi;
    private Button rebootBtn2;
    private TextView tvRaiseSRText;
    private TextView tvStillRaiseSrText;
    private static String dsl_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dsl_id = "";
        setContentView(R.layout.activity_quick_diagnostic);
        txtToolbarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
        txtToolbarTitle.setText("Quick Diagnostic");
        imgBack = (ImageView) findViewById(R.id.imgBack);
        imgBack.setOnClickListener(this);

        imgCheckConnectivity = findViewById(R.id.imgCheckConnectivity);
        imgCheckConnectivity.setOnClickListener(this);

        scrollDiagnostic = findViewById(R.id.scrollDiagnostic);

        all_done = findViewById(R.id.all_done);

        loader1 = findViewById(R.id.loader1);
        loader2 = findViewById(R.id.loader2);
        loader3 = findViewById(R.id.loader3);
        loader4 = findViewById(R.id.loader4);

        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);
        step4 = findViewById(R.id.step4);

        edit1 = findViewById(R.id.edit1);
        edit2 = findViewById(R.id.edit2);
        edit3 = findViewById(R.id.edit3);
        edit4 = findViewById(R.id.edit4);

        step1Name = findViewById(R.id.step1Name);
        step2Name = findViewById(R.id.step2Name);
        step3Name = findViewById(R.id.step3Name);
        step4Name = findViewById(R.id.step4Name);

        progressTv = findViewById(R.id.progressTv);
        rebootText = findViewById(R.id.rebootText);
        //rebootBtn = findViewById(R.id.rebootBtn);
        rebootBtn2 = findViewById(R.id.rebootBtn2);
        btRaiseSR = findViewById(R.id.btRaiseSR);
        btOptimizeWifi = findViewById(R.id.btOptimizeWifi);
        tvRaiseSRText = findViewById(R.id.tvRaiseSRText);
        tvStillRaiseSrText = findViewById(R.id.tvStillRaiseSrText);
        btRaiseSR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(dsl_id))
                    showCustomDialog();
            }
        });
        btOptimizeWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (NetworkUtils.isNetworkConnected(QuickDiagnosticNew.this)) {
                    showCustomDialogOptimize();
                } else  {
                    Toast.makeText(QuickDiagnosticNew.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*rebootBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //If internet connected then API call
                if (NetworkUtils.isNetworkConnected(QuickDiagnostic.this)) {
                    rebootRouter();
                } else  {
                    Toast.makeText(QuickDiagnostic.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });*/
        rebootBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //If internet connected then API call
                if (NetworkUtils.isNetworkConnected(QuickDiagnosticNew.this)) {
                    rebootRouter();
                } else {
                    Toast.makeText(QuickDiagnosticNew.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //rebootBtn.setVisibility(View.GONE);
        rebootText.setVisibility(View.GONE);

        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);

        apiKey = dduSharedPref.getString("apiKey", "");

        //Managing UI in case of orientation change
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) scrollDiagnostic.getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, R.id.layout_toolbar);
            scrollDiagnostic.setLayoutParams(lp);

        } else {
            // In portrait
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) scrollDiagnostic.getLayoutParams();
            lp.removeRule(RelativeLayout.BELOW);
            scrollDiagnostic.setLayoutParams(lp);
        }

        if (!NetworkUtils.isNetworkConnected(QuickDiagnosticNew.this)) {
            Toast.makeText(QuickDiagnosticNew.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) scrollDiagnostic.getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, R.id.layout_toolbar);
            scrollDiagnostic.setLayoutParams(lp);

        } else {
            // In portrait
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) scrollDiagnostic.getLayoutParams();
            lp.removeRule(RelativeLayout.BELOW);
            scrollDiagnostic.setLayoutParams(lp);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBack: {
                onBackPressed();
                break;
            }
            case R.id.imgCheckConnectivity: {
                if (!MethodsUtil.isRebootCacheOver(getApplicationContext())) {
                    Toast.makeText(this, "Please wait 3 minutes for the link to stabilize.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!NetworkUtils.isNetworkConnected(QuickDiagnosticNew.this)) {
                    Toast.makeText(QuickDiagnosticNew.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                    return;
                }

                imgCheckConnectivity.setEnabled(false);


                initDiagnostic();
                initNetworkOutage();
                callQuickDiagnosticApiStep1();

                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        QuickDiagnosticNew.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }


    private void callQuickDiagnosticApiStep1() {
        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial", userSerial);
            mainJObject.put("step", "step_1");
            mainJObject.put("version", BuildConfig.VERSION_NAME);

        } catch (JSONException ignored) {
        }
        final String mRequestBody = mainJObject.toString();

        try {

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + QuickDiagnosticEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // UIUtils.dismissDialog(mDialog);
                            if (response != null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                String state = response.optString("state");
                                String srButton = response.optString("SRButton");

                                if (srButton.equalsIgnoreCase("Show")) {
                                    btRaiseSR.setVisibility(View.VISIBLE);
                                    dsl_id = response.optString("dsl_id");
                                    String apiKeyDslId = md5(dsl_id + Constant.STATIC_KEY);
                                    SharedPreferences loginPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
                                    loginPref.edit().putString("apiKeyDslId", apiKeyDslId).apply();
                                } else {
                                    btRaiseSR.setVisibility(View.GONE);
                                }
                                if (MethodsUtil.isRebootCacheOver(getApplicationContext())) {
                                    rebootBtn2.setEnabled(true);
                                } else {
                                    rebootBtn2.setEnabled(false);
                                }

                                String rebootBtn = response.optString("reebot_b");
                                if (rebootBtn.equalsIgnoreCase("Show")) {
                                    rebootBtn2.setVisibility(View.VISIBLE);
                                } else {
                                    rebootBtn2.setVisibility(View.GONE);
                                }

                                if (statusCode.equals("200")) {
                                    activateNetworkOutage(msg);
                                    initDataProfileStatus();
                                    callQuickDiagnosticApiStep2();
                                } else if (statusCode.equals("201")) {
                                    deactivateNetworkOutage(msg);
                                } else {
                                    deactivateNetworkOutage();
                                    Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            imgCheckConnectivity.setEnabled(true);
                            //If some server error occurs then check how many seconds has passed and work accordingly
                            //imgCheckConnectivity.setEnabled(true);
                            deactivateNetworkOutage();

                            if (error.networkResponse!=null){
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                if (error instanceof TimeoutError) {
                                    Toast.makeText(QuickDiagnosticNew.this, R.string.timeout, Toast.LENGTH_SHORT).show();
                                } else if (error instanceof NoConnectionError) {
                                    Toast.makeText(QuickDiagnosticNew.this, R.string.error_noconnection, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(QuickDiagnosticNew.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(QuickDiagnosticNew.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }
    private void callQuickDiagnosticApiStep2() {
        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial", userSerial);
            mainJObject.put("step", "step_2");
            mainJObject.put("version", BuildConfig.VERSION_NAME);

        } catch (JSONException ignored) {
        }
        final String mRequestBody = mainJObject.toString();

        try {

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + QuickDiagnosticEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // UIUtils.dismissDialog(mDialog);
                            if (response != null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                String state = response.optString("state");
                                String srButton = response.optString("SRButton");

                                if (srButton.equalsIgnoreCase("Show")) {
                                    btRaiseSR.setVisibility(View.VISIBLE);
                                    dsl_id = response.optString("dsl_id");
                                    String apiKeyDslId = md5(dsl_id + Constant.STATIC_KEY);
                                    SharedPreferences loginPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
                                    loginPref.edit().putString("apiKeyDslId", apiKeyDslId).apply();
                                } else {
                                    btRaiseSR.setVisibility(View.GONE);
                                }
                                if (MethodsUtil.isRebootCacheOver(getApplicationContext())) {
                                    rebootBtn2.setEnabled(true);
                                } else {
                                    rebootBtn2.setEnabled(false);
                                }
                                String rebootBtn = response.optString("reebot_b");
                                if (rebootBtn.equalsIgnoreCase("Show")) {
                                    rebootBtn2.setVisibility(View.VISIBLE);
                                } else {
                                    rebootBtn2.setVisibility(View.GONE);
                                }

                                if (statusCode.equals("200")) {
                                    activateDataProfileStatus(msg);
                                    initLineStatus();
                                    callQuickDiagnosticApiStep3();
                                } else if (statusCode.equals("201")) {
                                    deactivateDataProfileStatus(msg);
                                } else {
                                    deactivateDataProfileStatus();
                                    Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            //If some server error occurs then check how many seconds has passed and work accordingly
                            imgCheckConnectivity.setEnabled(true);

                            deactivateDataProfileStatus();

                            if (error.networkResponse!=null){
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                if (error instanceof TimeoutError) {
                                    Toast.makeText(QuickDiagnosticNew.this, R.string.timeout, Toast.LENGTH_SHORT).show();
                                } else if (error instanceof NoConnectionError) {
                                    Toast.makeText(QuickDiagnosticNew.this, R.string.error_noconnection, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(QuickDiagnosticNew.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(QuickDiagnosticNew.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }
    private void callQuickDiagnosticApiStep3() {
        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial", userSerial);
            mainJObject.put("step", "step_3");
            mainJObject.put("version", BuildConfig.VERSION_NAME);

        } catch (JSONException ignored) {
        }
        final String mRequestBody = mainJObject.toString();

        try {

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + QuickDiagnosticEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // UIUtils.dismissDialog(mDialog);
                            if (response != null) {

                                isSuccessful = true;

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                String state = response.optString("state");
                                String srButton = response.optString("SRButton");

                                if (srButton.equalsIgnoreCase("Show")) {
                                    btRaiseSR.setVisibility(View.VISIBLE);
                                    dsl_id = response.optString("dsl_id");
                                    String apiKeyDslId = md5(dsl_id + Constant.STATIC_KEY);
                                    SharedPreferences loginPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
                                    loginPref.edit().putString("apiKeyDslId", apiKeyDslId).apply();
                                } else {
                                    btRaiseSR.setVisibility(View.GONE);
                                }
                                if (MethodsUtil.isRebootCacheOver(getApplicationContext())) {
                                    rebootBtn2.setEnabled(true);
                                } else {
                                    rebootBtn2.setEnabled(false);
                                }
                                String rebootBtn = response.optString("reebot_b");
                                if (rebootBtn.equalsIgnoreCase("Show")) {
                                    rebootBtn2.setVisibility(View.VISIBLE);
                                } else {
                                    rebootBtn2.setVisibility(View.GONE);
                                }

                                if (statusCode.equals("200")) {
                                    activateLineStatus(msg);
                                    initModemConectivity();
                                    callQuickDiagnosticApiStep4();
                                } else if (statusCode.equals("201")) {
                                    deactivateLineStatus(msg);
                                } else {
                                    deactivateLineStatus();
                                    Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            //If some server error occurs then check how many seconds has passed and work accordingly
                            imgCheckConnectivity.setEnabled(true);

                            deactivateLineStatus();

                            if (error.networkResponse!=null){
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                if (error instanceof TimeoutError) {
                                    Toast.makeText(QuickDiagnosticNew.this, R.string.timeout, Toast.LENGTH_SHORT).show();
                                } else if (error instanceof NoConnectionError) {
                                    Toast.makeText(QuickDiagnosticNew.this, R.string.error_noconnection, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(QuickDiagnosticNew.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(QuickDiagnosticNew.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }
    private void callQuickDiagnosticApiStep4() {
        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial", userSerial);
            mainJObject.put("step", "step_4");
            mainJObject.put("version", BuildConfig.VERSION_NAME);

        } catch (JSONException ignored) {
        }

        try {

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + QuickDiagnosticEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            if (response != null) {
                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                String state = response.optString("state");
                                String srButton = response.optString("SRButton");
                                String optimize_wifi = response.optString("optimize_wifi");
                                if (optimize_wifi.equalsIgnoreCase("Show")){
                                    btOptimizeWifi.setVisibility(View.VISIBLE);
                                }else {
                                    btOptimizeWifi.setVisibility(View.GONE);
                                }
                                if (srButton.equalsIgnoreCase("Show")) {
                                    btRaiseSR.setVisibility(View.VISIBLE);
                                    dsl_id = response.optString("dsl_id");
                                    String apiKeyDslId = md5(dsl_id + Constant.STATIC_KEY);
                                    SharedPreferences loginPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
                                    loginPref.edit().putString("apiKeyDslId", apiKeyDslId).apply();
                                } else {
                                    btRaiseSR.setVisibility(View.GONE);
                                }
                                if (MethodsUtil.isRebootCacheOver(getApplicationContext())) {
                                    rebootBtn2.setEnabled(true);
                                } else {
                                    rebootBtn2.setEnabled(false);
                                }

                                String bottom_text = response.optString("bottom_text");
                                if (!TextUtils.isEmpty(bottom_text))
                                {
                                    tvStillRaiseSrText.setText(bottom_text);
                                }else {
                                    tvStillRaiseSrText.setText("");
                                }

                                String rebootBtn = response.optString("reebot_b");
                                if (rebootBtn.equalsIgnoreCase("Show")) {
                                    rebootBtn2.setVisibility(View.VISIBLE);
                                } else {
                                    rebootBtn2.setVisibility(View.GONE);
                                }

                                if (statusCode.equals("200")) {
                                    activateModemConnectivity(msg);
                                    allDoneStep();
                                } else if (statusCode.equals("201")) {
                                    deactivateModemConnectivity(msg);
                                } else {
                                    deactivateModemConnectivity();
                                    Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            //If some server error occurs then check how many seconds has passed and work accordingly
                            imgCheckConnectivity.setEnabled(true);

                            deactivateModemConnectivity();

                            if (error.networkResponse!=null){
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                if (error instanceof TimeoutError) {
                                    Toast.makeText(QuickDiagnosticNew.this, R.string.error_timeout, Toast.LENGTH_SHORT).show();
                                } else if (error instanceof NoConnectionError) {
                                    Toast.makeText(QuickDiagnosticNew.this, R.string.error_noconnection, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(QuickDiagnosticNew.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(QuickDiagnosticNew.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    private void allDoneStep() {
        all_done.setVisibility(View.VISIBLE);
        tvStillRaiseSrText.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) scrollDiagnostic.getLayoutParams();
        lp.addRule(RelativeLayout.BELOW, R.id.layout_toolbar);
        scrollDiagnostic.setLayoutParams(lp);
    }

    private void initDiagnostic() {
        all_done.setVisibility(View.GONE);
        tvStillRaiseSrText.setVisibility(View.GONE);

        loader2.setVisibility(View.GONE);
        loader3.setVisibility(View.GONE);
        loader4.setVisibility(View.GONE);

        step2Name.setText("Data Profile Status");
        step4Name.setText("Modem Connectivity");
        step3Name.setText("Line Status");

        edit1.setVisibility(View.GONE);
        edit2.setVisibility(View.GONE);
        edit3.setVisibility(View.GONE);
        edit4.setVisibility(View.GONE);

        step1.setVisibility(View.GONE);
        step2.setVisibility(View.GONE);
        step3.setVisibility(View.GONE);
        step4.setVisibility(View.GONE);

        //rebootBtn.setVisibility(View.GONE);
        rebootText.setVisibility(View.GONE);

        loader1.setVisibility(View.VISIBLE);
        step1Name.setText("Checking Network Outage");

    }

    private void initNetworkOutage() {
        loader1.setVisibility(View.VISIBLE);
        step1Name.setText("Checking Network Outage");
    }
    private void initDataProfileStatus() {
        loader2.setVisibility(View.VISIBLE);
        step2Name.setText("Checking Data Profile Status");
    }
    private void initLineStatus() {
        loader3.setVisibility(View.VISIBLE);
        step3Name.setText("Checking Line Status");
    }
    private void initModemConectivity() {
        loader4.setVisibility(View.VISIBLE);
        step4Name.setText("Checking Modem Connectivity");
    }
    private void activateNetworkOutage(String msg) {
        step = "step1";
        step1Name.setText("Checked Network Outage");
        loader1.setVisibility(View.GONE);
        step1.setImageResource(R.drawable.ic_check_circle);
        step1.setVisibility(View.VISIBLE);
        edit1.setText(msg);
        edit1.setTextColor(getResources().getColor(R.color.greenTextColor));
        edit1.setVisibility(View.VISIBLE);
    }
    private void activateDataProfileStatus(String msg) {
        step = "step2";
        loader2.setVisibility(View.GONE);
        step2Name.setText("Checked Data Profile Status");
        step2.setImageResource(R.drawable.ic_check_circle);
        step2.setVisibility(View.VISIBLE);
        edit2.setText(msg);
        edit2.setTextColor(getResources().getColor(R.color.greenTextColor));
        edit2.setVisibility(View.VISIBLE);
    }
    private void activateLineStatus(String msg) {
        step = "step3";
        loader3.setVisibility(View.GONE);
        step3Name.setText("Checked Line Status");
        step3.setImageResource(R.drawable.ic_check_circle);
        step3.setVisibility(View.VISIBLE);
        edit3.setText(msg);
        edit3.setTextColor(getResources().getColor(R.color.greenTextColor));
        edit3.setVisibility(View.VISIBLE);
    }
    private void activateModemConnectivity(String msg) {
        step = "step1";
        step4Name.setText("Checked Modem Connectivity");
        loader4.setVisibility(View.GONE);
        step4.setImageResource(R.drawable.ic_check_circle);
        step4.setVisibility(View.VISIBLE);
        edit4.setText(msg);
        edit4.setTextColor(getResources().getColor(R.color.greenTextColor));
        edit4.setVisibility(View.VISIBLE);
    }

    private void deactivateNetworkOutage(String msg) {
        step = "step1";
        step1Name.setText("Checked Network Outage");
        loader1.setVisibility(View.GONE);
        step1.setImageResource(R.drawable.ic_cancel);
        step1.setVisibility(View.VISIBLE);
        edit1.setText(msg);
        edit1.setTextColor(getResources().getColor(R.color.colorPrimary));
        edit1.setVisibility(View.VISIBLE);
    }
    private void deactivateNetworkOutage() {
        step = "step1";
//        step1Name.setText("Checked Network Outage");
        loader1.setVisibility(View.GONE);
        step1.setImageResource(R.drawable.ic_cancel);
        step1.setVisibility(View.VISIBLE);
//        edit1.setTextColor(getResources().getColor(R.color.colorPrimary));
//        edit1.setVisibility(View.VISIBLE);
    }
    private void deactivateDataProfileStatus(String msg) {

        step = "step2";
        loader2.setVisibility(View.GONE);
        step2Name.setText("Checked Data Profile Status");
        step2.setImageResource(R.drawable.ic_cancel);
        step2.setVisibility(View.VISIBLE);
        edit2.setText(msg);
        edit2.setTextColor(getResources().getColor(R.color.colorPrimary));
        edit2.setVisibility(View.VISIBLE);
    }
    private void deactivateDataProfileStatus() {

        step = "step2";
        loader2.setVisibility(View.GONE);
        step2Name.setText("Checked Data Profile Status");
        step2.setImageResource(R.drawable.ic_cancel);
        step2.setVisibility(View.VISIBLE);
//        edit2.setText(msg);
//        edit2.setTextColor(getResources().getColor(R.color.colorPrimary));
//        edit2.setVisibility(View.VISIBLE);
    }
    private void deactivateLineStatus(String msg) {
        step = "step3";
        loader3.setVisibility(View.GONE);
        step3Name.setText("Checked Line Status");
        step3.setImageResource(R.drawable.ic_cancel);
        step3.setVisibility(View.VISIBLE);
        edit3.setText(msg);
        edit3.setTextColor(getResources().getColor(R.color.colorPrimary));
        edit3.setVisibility(View.VISIBLE);
    }
    private void deactivateLineStatus() {
        step = "step3";
        loader3.setVisibility(View.GONE);
        step3Name.setText("Checked Line Status");
        step3.setImageResource(R.drawable.ic_cancel);
        step3.setVisibility(View.VISIBLE);
//        edit3.setText(msg);
//        edit3.setTextColor(getResources().getColor(R.color.colorPrimary));
//        edit3.setVisibility(View.VISIBLE);
    }
    private void deactivateModemConnectivity(String msg) {
        step = "step1";
        step4Name.setText("Checked Modem Connectivity");
        loader4.setVisibility(View.GONE);
        step4.setImageResource(R.drawable.ic_cancel);
        step4.setVisibility(View.VISIBLE);
        edit4.setText(msg);
        edit4.setTextColor(getResources().getColor(R.color.colorPrimary));
        edit4.setVisibility(View.VISIBLE);
    }
    private void deactivateModemConnectivity() {
        step = "step1";
        step4Name.setText("Checked Modem Connectivity");
        loader4.setVisibility(View.GONE);
        step4.setImageResource(R.drawable.ic_cancel);
        step4.setVisibility(View.VISIBLE);
//        edit4.setText(msg);
//        edit4.setTextColor(getResources().getColor(R.color.colorPrimary));
//        edit4.setVisibility(View.VISIBLE);
    }

    //If some error occurs in case of 4th step then reboot button in shown so that user can reboot the router
    private void rebootRouter() {
        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial", userSerial);
        } catch (JSONException ignored) {
        }

        try {
            mDialog = UIUtils.showProgressDialog(QuickDiagnosticNew.this);
            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + reebootRouterEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            if (response != null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                if (statusCode.equals("200")) {
                                    Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
                                    MethodsUtil.setRebootCacheTime(getApplicationContext(), 3);
                                    rebootBtn2.setEnabled(false);
                                } else {
                                    Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(QuickDiagnosticNew.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(QuickDiagnosticNew.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    //When clicked on Register showing dialog to inform user to connect to home network for registering
    private void showCustomDialog() {
        final Dialog dialog = new Dialog(QuickDiagnosticNew.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.txt_title_dialog);
        title.setVisibility(GONE);

        TextView msg = (TextView) dialog.findViewById(R.id.txt_msg_dialog);
        msg.setText("Do you want to raise SR ?");

        TextInputEditText otpText = dialog.findViewById(R.id.inputText);
        otpText.setVisibility(View.GONE);

        Button positiveDialogButton = (Button) dialog.findViewById(R.id.btn_yes);
        positiveDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (NetworkUtils.isNetworkConnected(QuickDiagnosticNew.this)) {
                    callRaiseSRApi();
                } else {
                    Toast.makeText(QuickDiagnosticNew.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button negativeDialogButton = (Button) dialog.findViewById(R.id.btn_no);
        negativeDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void callRaiseSRApi() {
        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("dsl_id", dsl_id);
            mainJObject.put("version", BuildConfig.VERSION_NAME);

        } catch (JSONException ignored) {
        }

        try {
            mDialog = UIUtils.showProgressDialog(QuickDiagnosticNew.this);
            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + raiseSRUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            if (response != null) {

                                isSuccessful = true;

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                tvRaiseSRText.setVisibility(View.VISIBLE);

                                if (statusCode.equals("200")) {
                                    btRaiseSR.setVisibility(View.VISIBLE);
                                    btRaiseSR.setEnabled(false);
                                    btRaiseSR.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));

                                    JSONObject jsonObject = response.optJSONObject("data");
                                    if (jsonObject != null) {
                                        String problemId = jsonObject.optString("problemId");
                                        String displayMsg = "Your Service Request Number is " + problemId + ".";
                                        tvRaiseSRText.setText(displayMsg);
                                    }
                                } else if (statusCode.equals("201")) {
                                    btRaiseSR.setVisibility(View.VISIBLE);
                                    btRaiseSR.setEnabled(false);
                                    btRaiseSR.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
                                    tvRaiseSRText.setText(msg);
                                } else {
                                    //Toast.makeText(QuickDiagnostic.this, msg, Toast.LENGTH_SHORT).show();
                                    tvRaiseSRText.setText(msg);
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
                                Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(QuickDiagnosticNew.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
                    String apiKeyDslId = dduSharedPref.getString("apiKeyDslId", "");
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("key", apiKeyDslId);
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
            Toast.makeText(QuickDiagnosticNew.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }


    private void showCustomDialogOptimize() {
        final Dialog dialog = new Dialog(QuickDiagnosticNew.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.txt_title_dialog);
        title.setVisibility(GONE);

        TextView msg = (TextView) dialog.findViewById(R.id.txt_msg_dialog);
        msg.setText("WiFi signal may interrupt for few seconds");

        TextInputEditText otpText = dialog.findViewById(R.id.inputText);
        otpText.setVisibility(View.GONE);

        Button positiveDialogButton = (Button) dialog.findViewById(R.id.btn_yes);
        positiveDialogButton.setText("Continue");
        positiveDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (NetworkUtils.isNetworkConnected(QuickDiagnosticNew.this)) {
                    resetOptimizeNetwork();
                } else {
                    Toast.makeText(QuickDiagnosticNew.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button negativeDialogButton = (Button) dialog.findViewById(R.id.btn_no);
        negativeDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void resetOptimizeNetwork() {

        Snackbar.make(btOptimizeWifi, R.string.progressTvMsg, Snackbar.LENGTH_LONG).show();
//        progressTv.setVisibility(View.VISIBLE);
//        progressTv.setText(R.string.progressTvMsg);
        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial",userSerial);
            mainJObject.put("version", BuildConfig.VERSION_NAME);
        } catch (JSONException ignored) {
        }

        try {
            mDialog = UIUtils.showProgressDialog(QuickDiagnosticNew.this);
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + getoptimizeNetworkResetUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,mainJObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            if(response!=null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                if(statusCode.equals("200")){
                                    btOptimizeWifi.setEnabled(false);
                                    btOptimizeWifi.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
                                    Snackbar snackbar = Snackbar.make(btOptimizeWifi, R.string.successProgMsgQD, Snackbar.LENGTH_INDEFINITE)
                                            .setActionTextColor(getResources().getColor(R.color.colorPrimary))
                                            .setAction("OK", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    v.setVisibility(GONE);
                                                }
                                            });
                                    TextView snackbarActionTextView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_action );
                                    Typeface bold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
                                    snackbarActionTextView.setTypeface(bold);
                                    snackbar.show();

//                                    progressTv.setVisibility(View.VISIBLE);
//                                    progressTv.setText(R.string.successProgMsg);
//                                    Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();

                                    /*new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent =new Intent(QuickDiagnosticNew.this,MainMenuActivity.class);
                                            startActivity(intent);
                                            finish();
                                            QuickDiagnosticNew.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
                                        }
                                    },3000);*/


                                }else {
//                                    progressTv.setVisibility(View.GONE);
                                    Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            UIUtils.dismissDialog(mDialog);
//                            progressTv.setVisibility(View.GONE);
                            if (error.networkResponse!=null){
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(QuickDiagnosticNew.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(QuickDiagnosticNew.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(QuickDiagnosticNew.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    private static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException ignored) {
        }
        return "";
    }
}
