package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.Util.KeyboardUtils;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
    In Registration, firstly wifi connectivity is checked if user is connected to wifi then its ip address is fetched
    and all other information entered by user is posted in api body including io address and on success otp dialog appears
    which auto reads the otp or self enter, and on submiting OTP api is called to verify the user and on success user is redirected
    to login screen.
 */
public class Feedback extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = Feedback.class.getSimpleName();
    private ImageView imgBack, imgThankYou;
    private TextView txtToolbarTitle;
    private Button btnSubmit;
    private Dialog mDialog;
    private String getOptionsEndUrl = "v1/get_options";
    private String contactEndUrl = "v1/contact";
    String userSerial, apiKey;
    private boolean isKeyboardVisible;
    private AutoCompleteTextView actvQuery;
    private TextInputEditText etQueryFeedback;
    private EditText etFeedback;
    private ArrayList<String> queriesArrrayString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact_us);

        getOptions();

        etQueryFeedback = findViewById(R.id.etQueryFeedback);
        etFeedback = findViewById(R.id.etFeedback);
        actvQuery = findViewById(R.id.actvQuery);
        bindViewEditSpinner();

        // Create a border programmatically
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(Color.GREEN);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(3);

        // Assign the created border to EditText widget
        etFeedback.setBackground(shape);

        txtToolbarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
        txtToolbarTitle.setText("Feedback");
        btnSubmit = findViewById(R.id.btnSubmit);
        imgBack = findViewById(R.id.imgBack);
        imgThankYou = findViewById(R.id.imgThankYou);

        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(Feedback.this, userSerialEncrypted);
        apiKey = dduSharedPref.getString("apiKey", "");

        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {
                isKeyboardVisible = isVisible;
            }
        });
        imgBack.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
    }

    private void bindViewEditSpinner() {
        actvQuery.setCursorVisible(false);
        actvQuery.setFocusable(false);
        actvQuery.setClickable(true);
        actvQuery.setSelected(false);
        actvQuery.setKeyListener(null);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.custom_simple_spinner_dropdown_item, queriesArrrayString);
        actvQuery.setAdapter(adapter);
        actvQuery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actvQuery.showDropDown();
            }
        });
        actvQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actvQuery.showDropDown();
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBack:
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
            case R.id.btnSubmit:
                if (NetworkUtils.isNetworkConnected(Feedback.this)) {
                    if (isValidated()) {
                        callContactApi();
                    }
                } else {
                    Toast.makeText(Feedback.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private boolean isValidated() {
        boolean flag = false;
        if (actvQuery != null && TextUtils.isEmpty(actvQuery.getText().toString().trim())) {
            Toast.makeText(this, "Fields can not be empty!", Toast.LENGTH_SHORT).show();
        }else if (etQueryFeedback != null && TextUtils.isEmpty(etQueryFeedback.getText().toString().trim())) {
            Toast.makeText(this, "Fields can not be empty!", Toast.LENGTH_SHORT).show();
        } else if (etQueryFeedback != null && !etQueryFeedback.getText().toString().matches("[a-zA-Z0-9.()@;:,!_&#?\n \\\\/-]*")) {
            Toast.makeText(this, "No special characters allowed", Toast.LENGTH_SHORT).show();
        } else {
            flag = true;
        }
        return flag;
    }

    private void getOptions() {
        JSONObject mainJObject = new JSONObject();

        try {
            SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
            String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
            userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);
            apiKey = dduSharedPref.getString("apiKey", "");

            mainJObject.put("dsl_id", userSerial);

        } catch (JSONException ignored) {
        }

        try {
            mDialog = UIUtils.showProgressDialog(Feedback.this);
            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + getOptionsEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            if (response != null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");

                                if (statusCode.equals("200")) {
                                    queriesArrrayString = new ArrayList<>();
                                    JSONObject jsonObject = response.optJSONObject("data");
                                    if (jsonObject != null) {
                                        JSONArray options = jsonObject.optJSONArray("options");
                                        for (int i = 0; i < options.length(); i++) {
                                            String opt = options.optString(i);
                                            queriesArrrayString.add(opt);
                                        }
                                        ArrayAdapter adapter = new ArrayAdapter<String>(Feedback.this, R.layout.custom_simple_spinner_dropdown_item, queriesArrrayString);
                                        actvQuery.setAdapter(adapter);
                                    }
                                } else if (statusCode.equals("201")) {
                                } else {
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
                                Toast.makeText(Feedback.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(Feedback.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(Feedback.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callContactApi() {

        JSONObject mainJObject = new JSONObject();

        try {
            mainJObject.put("dsl_id", userSerial);
            mainJObject.put("type", actvQuery.getText());
            mainJObject.put("query", etQueryFeedback.getText());
        } catch (JSONException ignored) {
        }

        try {
            mDialog = UIUtils.showProgressDialog(Feedback.this);
            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + contactEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            if (response != null) {
                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                if (statusCode.equals("200")) {
                                    actvQuery.setText("");
                                    etQueryFeedback.setText("");
                                    imgThankYou.setVisibility(View.VISIBLE);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            imgThankYou.setVisibility(View.GONE);
                                        }
                                    }, 5000);

                                } else {
                                    UIUtils.dismissDialog(mDialog);
                                    Toast.makeText(Feedback.this, msg, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(Feedback.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(Feedback.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    params.put("key", apiKey);
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
        } catch (Exception e) { // for caught any exception during the excecution of the service

            UIUtils.dismissDialog(mDialog);
            Toast.makeText(Feedback.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Feedback.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }
}
