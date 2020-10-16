package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.Util.MethodsUtil;
import com.airtel.xstreamfiber.Util.NetworkUtils;
import com.airtel.xstreamfiber.Util.UIUtils;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.view.View.GONE;

/*
    In Optimize wifi network, data is fetched from MainMenuActivity like channel 2.4G number and channel 5G number
    and displayed on the screen. When user clicks on Optimize button then APi is hit to refresh the channel and the
    user is redirected to the mainMenu screen.
*/
public class OptimizeWifiNetwork extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgBack;
    private TextView tvChannel, tvChannel5G, progressTv, tv_channel, tv_channel5G;
    private String channelName, channel5G;
    private Button btnOptimizeNetworkReset;
    private Dialog mDialog;
    private String getoptimizeNetworkResetUrl="v1/change_channel";
    private String getDeviceInfoUrl="v1/device_info", logoutEndUrl = "v1/logout";
    private TextView txtToolbarTitle, channelTv;
    private String userSerial;
    private String apiKey;
    private LinearLayout llOptimize;
    private ScrollView scrollOptimizeWifi;
    private View channel2Card, channel5Card;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_optimize_wifi);
        txtToolbarTitle=(TextView)findViewById(R.id.txtToolbarTitle);
        txtToolbarTitle.setText("WiFi Optimization");
        imgBack=(ImageView)findViewById(R.id.imgBack);
        imgBack.setOnClickListener(this);
        btnOptimizeNetworkReset=(Button)findViewById(R.id.btnOptimizeNetworkReset);
        btnOptimizeNetworkReset.setOnClickListener(this);

        llOptimize = findViewById(R.id.llOptimize);
        channelTv = findViewById(R.id.channelTv);
        scrollOptimizeWifi = findViewById(R.id.scrollOptimizeWifi);

        channel2Card = findViewById(R.id.channel2Card);
        channel5Card = findViewById(R.id.channel5Card);

        tvChannel=(TextView)findViewById(R.id.tvChannel);
        tvChannel5G = (TextView) findViewById(R.id.tvChannel5G);

        tv_channel = findViewById(R.id.tv_channel);
        tv_channel5G = findViewById(R.id.tv_channel5G);

        progressTv = findViewById(R.id.progressTv);

        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);
        apiKey = dduSharedPref.getString("apiKey", "");

        preferences = getSharedPreferences(userSerial,MODE_PRIVATE);
        editor = preferences.edit();

        String channel_2G = preferences.getString("channelName","");
        String channel_5G = preferences.getString("channel5G","");

        //Checking if internet connected or not
        if (NetworkUtils.isNetworkConnected(OptimizeWifiNetwork.this)) {
            getDeviceInfo();
        } else  {
            Toast.makeText(OptimizeWifiNetwork.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
        }

//        if (channel_2G.isEmpty() && channel_5G.isEmpty())
//        {
//            //Checking if internet connected or not
//            if (NetworkUtils.isNetworkConnected(OptimizeWifiNetwork.this)) {
//                getDeviceInfo();
//            } else  {
//                Toast.makeText(OptimizeWifiNetwork.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
//            }
//        }
//        else {
//            if (channel_2G.isEmpty() || channel_2G.equals("")) {
//                tv_channel.setVisibility(View.GONE);
//                tvChannel.setVisibility(View.GONE);
//            } else {
//                tv_channel.setVisibility(View.VISIBLE);
//                tvChannel.setVisibility(View.VISIBLE);
//                tv_channel.setText(channel_2G);
//            }
//            if (channel_5G.isEmpty() || channel_5G.equals("")) {
//                tvChannel5G.setVisibility(View.GONE);
//                tv_channel5G.setVisibility(View.GONE);
//            } else {
//                tvChannel5G.setVisibility(View.VISIBLE);
//                tv_channel5G.setVisibility(View.VISIBLE);
//                tv_channel.setText(channel_5G);
//            }
//        }




    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBack: {
                onBackPressed();
                break;
            }case R.id.btnOptimizeNetworkReset: {
                //If internet connected then reset
                if (NetworkUtils.isNetworkConnected(OptimizeWifiNetwork.this)) {
                    showCustomDialog();
                } else  {
                    Toast.makeText(OptimizeWifiNetwork.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        OptimizeWifiNetwork.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }

    private void showCustomDialog() {
        final Dialog dialog = new Dialog(OptimizeWifiNetwork.this);
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
                if (NetworkUtils.isNetworkConnected(OptimizeWifiNetwork.this)) {
                    resetOptimizeNetwork();
                } else {
                    Toast.makeText(OptimizeWifiNetwork.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
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

    private void getDeviceInfo() {

        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial",userSerial);
            mainJObject.put("version", BuildConfig.VERSION_NAME);
        } catch (JSONException ignored) {
        }

        try {
            mDialog = UIUtils.showProgressDialog(OptimizeWifiNetwork.this);
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + getDeviceInfoUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,mainJObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            if(response!=null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                if(statusCode.equals("200")){

                                    JSONObject jsonObject=response.optJSONObject("data");

                                    String Model=jsonObject.optString("Model");
                                    String FirmwareVersion=jsonObject.optString("FirmwareVersion");
                                    String CPEHealth=jsonObject.optString("CPE Health");
                                    int UpTime=jsonObject.optInt("UpTime");

                                    String Channel=jsonObject.optString("Channel");
                                    String channel5G = jsonObject.optString("Channel_5G");


                                    if (Channel.isEmpty())
                                    {
                                        tv_channel.setVisibility(View.GONE);
                                        tvChannel.setVisibility(View.GONE);
                                        channel2Card.setVisibility(View.GONE);
                                    }
                                    else {
                                        tv_channel.setVisibility(View.VISIBLE);
                                        tvChannel.setVisibility(View.VISIBLE);
                                        tvChannel.setText(Channel);
                                        channel2Card.setVisibility(View.VISIBLE);
                                    }

                                    if(channel5G.isEmpty())
                                    {
                                        tvChannel5G.setVisibility(View.GONE);
                                        tv_channel5G.setVisibility(View.GONE);
                                        channel5Card.setVisibility(View.GONE);
                                    }
                                    else {
                                        tvChannel5G.setVisibility(View.VISIBLE);
                                        tv_channel5G.setVisibility(View.VISIBLE);
                                        tvChannel5G.setText(channel5G);
                                        channel5Card.setVisibility(View.VISIBLE);
                                    }


                                    int p1 = UpTime % 60;
                                    int p2 = UpTime / 60;
                                    int p3 = p2 % 60;

                                    p2 = p2 / 60;
                                    String upTime=(p2 + ":" + p3 + ":" + p1);

                                    editor.putString("Model", Model);
                                    editor.putString("FirmwareVersion", FirmwareVersion);
                                    editor.putString("CPEHealth", CPEHealth);
                                    editor.putString("channelName", Channel);
                                    editor.putString("upTime", upTime);
                                    editor.putString("channel5G", channel5G);
                                    editor.apply();


                                    //30 May 2020
                                    String logout = jsonObject.optString("logout");
                                    if (!logout.equals("") && !logout.isEmpty()) {
                                        if (logout.equalsIgnoreCase("yes")){
                                            logoutMethod();
                                        }else if (logout.equalsIgnoreCase("no")){
                                            logoutPart2();
                                        }
                                    }
                                }else {
                                    UIUtils.dismissDialog(mDialog);
                                    Toast.makeText(OptimizeWifiNetwork.this, msg, Toast.LENGTH_SHORT).show();
                                    editor.putString("APIDeviceInfo", "NotCalled").apply();
                                }

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            UIUtils.dismissDialog(mDialog);
                            editor.putString("APIDeviceInfo", "NotCalled").apply();
                            if (error.networkResponse!=null){
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(OptimizeWifiNetwork.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(OptimizeWifiNetwork.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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

        } catch (Exception ignored) {
        }
    }

    private void resetOptimizeNetwork() {

        progressTv.setVisibility(View.VISIBLE);
        progressTv.setText(R.string.progressTvMsg);

        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial",userSerial);
            mainJObject.put("version", BuildConfig.VERSION_NAME);
        } catch (JSONException ignored) {
        }

        try {
            mDialog = UIUtils.showProgressDialog(OptimizeWifiNetwork.this);
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
                                    progressTv.setVisibility(View.VISIBLE);
                                    progressTv.setText(R.string.successProgMsg);
                                    Toast.makeText(OptimizeWifiNetwork.this, msg, Toast.LENGTH_SHORT).show();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent =new Intent(OptimizeWifiNetwork.this,MainMenuActivity.class);
                                            startActivity(intent);
                                            finish();
                                            OptimizeWifiNetwork.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
                                        }
                                    },3000);


                                }else {
                                    progressTv.setVisibility(View.GONE);
                                    Toast.makeText(OptimizeWifiNetwork.this, msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            UIUtils.dismissDialog(mDialog);
                            progressTv.setVisibility(View.GONE);
                            if (error.networkResponse!=null){
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(OptimizeWifiNetwork.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(OptimizeWifiNetwork.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(OptimizeWifiNetwork.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }




    private void logoutPart2() {
        if (MethodsUtil.isLoginSessionOver(getApplicationContext())) {
            logoutMethod();
        } else {
            if (NetworkUtils.isNetworkConnected(OptimizeWifiNetwork.this)) {
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

        Intent intent = new Intent(OptimizeWifiNetwork.this, Login.class);
        startActivity(intent);
        finish();
        OptimizeWifiNetwork.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);

        Toast.makeText(OptimizeWifiNetwork.this, "Logging out due to technical reason.", Toast.LENGTH_LONG).show();
    }
}
