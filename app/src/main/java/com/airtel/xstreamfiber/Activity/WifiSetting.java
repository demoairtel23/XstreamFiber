package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
    In Wifi setting, getSSid api is called for fetching data from server if no ssid found then toast is shown of same,
    when user enters ssid and password and submitted then setSSid is called to set new ssid for the router and on success
    getSSid is again called to show the ssid to user.
 */
public class WifiSetting extends AppCompatActivity implements View.OnClickListener {

    private EditText editSSID;
    private EditText editPass;
    private Dialog mDialog;

    private String getSSIDsendUrl = "v1/ssid";
    //    private String setSsidPassEndUrl="v1/ssid/change";
    private String setSsidPassEndUrl = "v2/ssid/change";

    private Context context;

    private TextView txt2ghz, txt5ghz, txt2GHzHeading, txt5GHzHeading, txtSingleSSID, txtSingleHeading;
    private ImageView imgBack;
    private Button btnSubSSIDPass, btnSame, btnDifferent;
    private LinearLayout llWifiNameTypeOption;
    private RelativeLayout rlWifiNameType;
    private TextView txtToolbarTitle;
    private String userSerial;
    private String apiKey;
    TextInputLayout userNameLayout;
    private boolean isKeyboardVisible;
    private String hasDifferentName;
    private boolean isSelectedWifiType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wifi_setting);
        txtToolbarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
//        txtToolbarTitle.setText("Change WiFi Setting");
        txtToolbarTitle.setText("Change WiFi Credentials");

        editSSID = (EditText) findViewById(R.id.editSSID);
        editPass = (EditText) findViewById(R.id.editPass);
        btnSubSSIDPass = (Button) findViewById(R.id.btnSubSSIDPass);
        btnSubSSIDPass.setOnClickListener(this);
        txt2GHzHeading = (TextView) findViewById(R.id.txt2GHzHeading);
        txt5GHzHeading = (TextView) findViewById(R.id.txt5GHzHeading);
        txt2ghz = (TextView) findViewById(R.id.txt2ghz);
        txt5ghz = (TextView) findViewById(R.id.txt5ghz);
        txtSingleHeading = (TextView) findViewById(R.id.txtSingleHeading);
        txtSingleSSID = (TextView) findViewById(R.id.txtSingleSSID);
        btnSame = findViewById(R.id.btnSame);
        btnDifferent = findViewById(R.id.btnDifferent);
        llWifiNameTypeOption = findViewById(R.id.llWifiNameTypeOption);
        rlWifiNameType = findViewById(R.id.rlWifiNameType);
        userNameLayout = findViewById(R.id.userNameLayout);
        imgBack = (ImageView) findViewById(R.id.imgBack);

        btnSame.setOnClickListener(this);
        btnDifferent.setOnClickListener(this);
        imgBack.setOnClickListener(this);

        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);
        apiKey = dduSharedPref.getString("apiKey", "");


        //Managing UI in case of orientation change
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) userNameLayout.getLayoutParams();
            mlp.topMargin = 0;
            userNameLayout.setLayoutParams(mlp);

        } else {
            // In portrait
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) userNameLayout.getLayoutParams();
            mlp.topMargin = 50;
            userNameLayout.setLayoutParams(mlp);
        }

        //If network connected call API
        if (NetworkUtils.isNetworkConnected(WifiSetting.this)) {
            getNetwork();
        } else {
            Toast.makeText(WifiSetting.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
        }

        SimpleDateFormat myFormat = new SimpleDateFormat("dd MM yyyy");
        String inputString1 = "23 01 1997";
        String inputString2 = "27 04 1997";

        try {
            Date date1 = myFormat.parse(inputString1);
            Date date2 = myFormat.parse(inputString2);
            long diff = date2.getTime() - date1.getTime();

        } catch (ParseException ignored) {
        }

        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {
                isKeyboardVisible = isVisible;
            }
        });
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
            case R.id.btnSubSSIDPass: {
                if (NetworkUtils.isNetworkConnected(WifiSetting.this)) {
                    setSSIDandPassword();
                } else {
                    Toast.makeText(WifiSetting.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.btnSame:
                btnDifferent.setBackgroundResource(R.color.light_gray_color);
                btnDifferent.setTextColor(getResources().getColor(R.color.btn_text_color));
                btnSame.setBackgroundResource(R.color.colorPrimary);
                btnSame.setTextColor(getResources().getColor(R.color.white));
                hasDifferentName = "";
                isSelectedWifiType = true;
                break;
            case R.id.btnDifferent:
                btnSame.setBackgroundResource(R.color.light_gray_color);
                btnSame.setTextColor(getResources().getColor(R.color.btn_text_color));
                btnDifferent.setBackgroundResource(R.color.colorPrimary);
                btnDifferent.setTextColor(getResources().getColor(R.color.white));
                hasDifferentName = "true";
                isSelectedWifiType = true;
                break;
        }
    }

    private void setSSIDandPassword() {
        if (!TextUtils.isEmpty(editSSID.getText().toString().trim()) && !TextUtils.isEmpty(editPass.getText().toString().trim())) {
            if (isTypeSelectedMethod() == 0) {
                Toast.makeText(WifiSetting.this, "Select WiFi Name Option", Toast.LENGTH_SHORT).show();
            } else {
                String enteredSSID = editSSID.getText().toString().trim();
                String enteredpass = editPass.getText().toString().trim();
                if (!(enteredpass.length() > 7 && enteredpass.length() < 21)) {
                    Toast.makeText(WifiSetting.this, "Password should be 8-20 characters.", Toast.LENGTH_SHORT).show();
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
                        if (isTypeSelectedMethod() == 1)
                            mainJObject.put("SSID_option", hasDifferentName);
                    } catch (JSONException ignored) {
                    }

                    try {
                        mDialog = UIUtils.showProgressDialog(WifiSetting.this);
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

                                                Toast.makeText(WifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                                                editSSID.setText("");
                                                editPass.setText("");
//                                            getNetwork();
                                                Toast.makeText(WifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(WifiSetting.this, MainMenuActivity.class));
                                                finish();
                                                WifiSetting.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);

                                            } else {
                                                Toast.makeText(WifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        UIUtils.dismissDialog(mDialog);
                                        if (error.networkResponse != null) {
                                            String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                            Toast.makeText(WifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (error instanceof TimeoutError) {
                                                Toast.makeText(WifiSetting.this, R.string.timeout, Toast.LENGTH_SHORT).show();
                                            } else if (error instanceof NoConnectionError) {
                                                Toast.makeText(WifiSetting.this, R.string.error_noconnection, Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(WifiSetting.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
                            }
                        });
                        queue.add(getRequest);

                    } catch (Exception e) { // for caught any exception during the excecution of the service

                        UIUtils.dismissDialog(mDialog);
                        Toast.makeText(WifiSetting.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else {
            Toast.makeText(WifiSetting.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private int isTypeSelectedMethod() {
        int flag = -1;
        if (llWifiNameTypeOption.getVisibility() == View.VISIBLE) {
            if (!isSelectedWifiType) {
                flag = 0;
            } else {
                flag = 1;
            }
        }
        return flag;
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
            mlp.topMargin = 50;
            userNameLayout.setLayoutParams(mlp);

        }
    }

    private void getNetwork() {

        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial", userSerial);
        } catch (JSONException ignored) {
        }
        final String mRequestBody = mainJObject.toString();

        try {
            mDialog = UIUtils.showProgressDialog(WifiSetting.this);
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


                                    String SSID_option = response.optString("SSID_option");
                                    if (SSID_option.trim().equalsIgnoreCase("show")) {
                                        llWifiNameTypeOption.setVisibility(View.VISIBLE);
                                        rlWifiNameType.setVisibility(View.VISIBLE);
                                    } else {
                                        llWifiNameTypeOption.setVisibility(View.GONE);
                                        rlWifiNameType.setVisibility(View.GONE);
                                    }

                                    JSONArray jsonArray = response.optJSONArray("data");
                                    ArrayList<SSIDModel> allObjArrayList = new ArrayList<>();

                                    for (int i = 0; i < jsonArray.length(); i++) {

                                        JSONObject newjb = jsonArray.optJSONObject(i);
                                        String SSID = newjb.optString("SSID");
                                        String instance = newjb.optString("Instance");
                                        String active = newjb.optString("active");

                                        //Filtering data if active status is true then add
                                        if (active.equals("true")) {
                                            SSIDModel ssidModel = new SSIDModel();
                                            ssidModel.setSSID(SSID);
                                            ssidModel.setInstance(instance);

                                            allObjArrayList.add(ssidModel);
                                        }

                                    }
                                    ArrayList<SSIDModel> filteredArrayList = new ArrayList<>();

                                    for (int i = 0; i < allObjArrayList.size(); i++) {
                                        SSIDModel ssidModel = allObjArrayList.get(i);

                                        String instance = ssidModel.getInstance();

                                        //Filtering data if instance 1 or 5 then add
                                        if (instance.equals("1") || instance.equals("5")) {
                                            filteredArrayList.add(ssidModel);
                                        }
                                    }

                                    if (filteredArrayList.size() == 0) {

                                        //If no data present then show SSID not available
                                        Toast.makeText(WifiSetting.this, "SSID not available", Toast.LENGTH_SHORT).show();

                                    } else if (filteredArrayList.size() == 1) {
                                        SSIDModel ssidModel = filteredArrayList.get(0);
                                        if (ssidModel.getInstance().equals("1")) {
                                            txtSingleHeading.setVisibility(View.VISIBLE);
                                            txtSingleSSID.setVisibility(View.VISIBLE);
                                            txtSingleHeading.setText("2.4 GHz");
                                            txtSingleSSID.setText(ssidModel.getSSID());
                                            editSSID.setText(ssidModel.getSSID());
                                        } else if (ssidModel.getInstance().equals("5")) {
                                            txtSingleHeading.setVisibility(View.VISIBLE);
                                            txtSingleSSID.setVisibility(View.VISIBLE);
                                            txtSingleHeading.setText("5 GHz");
                                            txtSingleSSID.setText(ssidModel.getSSID());
                                        }

                                    } else if (filteredArrayList.size() == 2) {
                                        for (int j = 0; j < filteredArrayList.size(); j++) {
                                            SSIDModel ssidModel = filteredArrayList.get(j);
                                            if (ssidModel.getInstance().equals("1")) {
                                                txt2GHzHeading.setVisibility(View.VISIBLE);
                                                txt2ghz.setText(ssidModel.getSSID());
                                                editSSID.setText(ssidModel.getSSID());
                                                txt2ghz.setVisibility(View.VISIBLE);

                                            } else if (ssidModel.getInstance().equals("5")) {
                                                txt5GHzHeading.setVisibility(View.VISIBLE);
                                                txt5ghz.setText(ssidModel.getSSID());
                                                txt5ghz.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(WifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            UIUtils.dismissDialog(mDialog);
                            if (error.networkResponse != null) {
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(WifiSetting.this, msg, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(WifiSetting.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(WifiSetting.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        WifiSetting.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }

    public void showPopupWindow(View view) {
        PopupWindow pw = new PopupWindow(LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_help_popup, null, false), WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        pw.showAsDropDown(view, view.getScrollX(), view.getScrollY());
    }
}
