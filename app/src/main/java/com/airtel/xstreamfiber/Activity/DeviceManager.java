package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/*
    In device manager, all data is fetched from the MainMenuActivity through shared preference and displayed here.
*/
public class DeviceManager extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgBack;
    private LinearLayout lnr_upgrade;
    private TextView txtUpgrade, txtCphealth,txtUpTime,txtModelNo,txtChannel5, txtChannel2,txtFirmware, simpleText, txtValChannel5, txtValChannel2;
    private Dialog mDialog;
    private String getDeviceInfoUrl="v1/device_info";
    private TextView txtToolbarTitle;
    private String userSerial;
    private String apiKey, upgrade;
    private SharedPreferences sharedPreferences;
    ScrollView scrollDeviceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manager);
       //Initializing all views
        txtToolbarTitle=(TextView)findViewById(R.id.txtToolbarTitle);
        txtToolbarTitle.setText("Device Manager");
        imgBack=(ImageView)findViewById(R.id.imgBack);
        imgBack.setOnClickListener(this);
        lnr_upgrade = findViewById(R.id.lnr_upgrade);
        txtUpgrade = findViewById(R.id.txtUpgrade);
        txtCphealth=(TextView)findViewById(R.id.txtCphealth);
        txtUpTime=(TextView)findViewById(R.id.txtUpTime);
        txtModelNo=(TextView)findViewById(R.id.txtModelNo);
        txtChannel2=(TextView)findViewById(R.id.txtChannel2);
        txtChannel5 = findViewById(R.id.txtChannel5);
        txtFirmware=(TextView)findViewById(R.id.txtFirmware);
        simpleText = findViewById(R.id.simpleText);
        txtValChannel5 = findViewById(R.id.txtValChannel5);
        txtValChannel2 = findViewById(R.id.txtValChannel2);
        scrollDeviceManager = findViewById(R.id.scrollDeviceManager);

        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);
        apiKey = dduSharedPref.getString("apiKey", "");

        sharedPreferences = getSharedPreferences(userSerial, MODE_PRIVATE);
        upgrade = sharedPreferences.getString("upgrade", "");

        if (upgrade.length() != 0)
        {
            lnr_upgrade.setVisibility(View.VISIBLE);
            txtUpgrade.setText(upgrade);
        }
        else
            lnr_upgrade.setVisibility(View.GONE);

        //In case of orientation change, managing UI
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) scrollDeviceManager.getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, R.id.layout_title);
            scrollDeviceManager.setLayoutParams(lp);

        } else {
            // In portrait
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) scrollDeviceManager.getLayoutParams();
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            lp.removeRule(RelativeLayout.BELOW);
            scrollDeviceManager.setLayoutParams(lp);
        }

        //Checking if internet connected or not
        if (NetworkUtils.isNetworkConnected(DeviceManager.this)) {
            getDeviceInfo();
        } else  {
            Toast.makeText(DeviceManager.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
        }

    }

    private void getDeviceInfo() {

        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial",userSerial);
            mainJObject.put("version", BuildConfig.VERSION_NAME);
        } catch (JSONException ignored) {
        }

        try {
            mDialog = UIUtils.showProgressDialog(DeviceManager.this);
            mDialog.setCancelable(false);

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
                                    String CPHealth=jsonObject.optString("CPE Health");
                                    String info=jsonObject.optString("info");
                                    int UpTime=jsonObject.optInt("UpTime");

                                    String Channel=jsonObject.optString("Channel");
                                    String channel5G = jsonObject.optString("Channel_5G");

                                   // Toast.makeText(DeviceManager.this, msg, Toast.LENGTH_SHORT).show();

                                    int p1 = UpTime % 60;
                                    int p2 = UpTime / 60;
                                    int p3 = p2 % 60;

                                    p2 = p2 / 60;

                                    txtModelNo.setText(Model);
                                    txtFirmware.setText(FirmwareVersion);
                                    txtCphealth.setText(CPHealth);
                                    txtUpTime.setText(p2 + ":" + p3 + ":" + p1);
                                    txtChannel2.setText("Channel_2.4 GHz: ");
                                    txtValChannel2.setText(Channel);
                                    txtChannel5.setText("Channel_5 GHz: ");
                                    txtValChannel5.setText(channel5G);

                                    if (!info.isEmpty())
                                    {
                                        simpleText.setText(info);
                                        simpleText.setVisibility(View.VISIBLE);
                                    }

                                    if (Channel.equals("") || Channel.isEmpty())
                                    {
                                        txtChannel2.setVisibility(View.GONE);
                                        txtValChannel2.setVisibility(View.GONE);
                                    }
                                    else {
                                        txtChannel2.setVisibility(View.VISIBLE);
                                        txtValChannel2.setVisibility(View.VISIBLE);
                                    }
                                  //  txtChannel2.setText("Channel_2.4 GHz: "+Channel);
                                    if(channel5G.equals("") || channel5G.isEmpty()){
                                        txtChannel5.setVisibility(View.GONE);
                                        txtValChannel5.setVisibility(View.GONE);
                                    }
                                    else {
                                        txtChannel5.setVisibility(View.VISIBLE);
                                        txtValChannel5.setVisibility(View.VISIBLE);
                                    }
                                   // txtChannel5.setText("Channel_5 GHz: "+channel5G);

                                }else {
                                    UIUtils.dismissDialog(mDialog);
                                    Toast.makeText(DeviceManager.this, msg, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(DeviceManager.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(DeviceManager.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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

            Toast.makeText(DeviceManager.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) scrollDeviceManager.getLayoutParams();
            lp.addRule(RelativeLayout.BELOW, R.id.layout_title);
            scrollDeviceManager.setLayoutParams(lp);
        } else {
            // In portrait
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) scrollDeviceManager.getLayoutParams();
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            lp.removeRule(RelativeLayout.BELOW);
            scrollDeviceManager.setLayoutParams(lp);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DeviceManager.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
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
}
