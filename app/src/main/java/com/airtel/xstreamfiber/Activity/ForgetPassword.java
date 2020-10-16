package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airtel.xstreamfiber.Apphelper.SmsListener;
import com.airtel.xstreamfiber.Apphelper.SmsReceiver;
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ForgetPassword extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "ForgetPassword";
    private TextView tvBack, tvCountdown;
    private ImageView airtelLogo;
    private TextInputEditText editMobileNo;
    private Button btnNext;
    //    private String getForgotPwdEndUrl = "v1/forgot_password";
    private String getForgotPwdEndUrl = "v2/forgot_password";
    //    private String getOtpVerifyEndUrl = "v1/otp_verify";
    private String getOtpVerifyEndUrl = "v2/otp_verify";
    private Dialog mDialog;
    private String mobile, key;
    private static StringBuilder hexString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        initViews();
        tvBack.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        //Managing UI in case of orientation change
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) airtelLogo.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            airtelLogo.setLayoutParams(params);
        } else {
            // In portrait
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) airtelLogo.getLayoutParams();
            params.setMargins(0, 70, 0, 70);
            airtelLogo.setLayoutParams(params);
        }


        if (!MethodsUtil.isOTPCacheOver(getApplicationContext())) {
            btnNext.setVisibility(View.VISIBLE);
            btnNext.setEnabled(false);
            btnNext.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));

            SharedPreferences sharedPreferences = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
            if (sharedPreferences.getLong("otp_cache_time", 0) != 0) {
                if (Calendar.getInstance().getTime().getTime() < (sharedPreferences.getLong("otp_cache_time", 0))) {
                    //reboot cache time is over
                    long t = sharedPreferences.getLong("otp_cache_time", 0) - Calendar.getInstance().getTime().getTime();
                    Calendar rem = Calendar.getInstance();
                    rem.setTimeInMillis(t);
                    int sec = rem.get(Calendar.SECOND);


                    CountDownTimer downTimer = new CountDownTimer(sec * 1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            tvCountdown.setVisibility(View.VISIBLE);
                            tvCountdown.setText("Please wait for " + (millisUntilFinished+1000)/1000 + " seconds");
                            //Toast.makeText(ForgetPassword.this, ""+millisUntilFinished/1000, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFinish() {
                            tvCountdown.setVisibility(View.GONE);
                            btnNext.setVisibility(View.VISIBLE);
                            btnNext.setEnabled(true);
                            btnNext.setBackground(getResources().getDrawable(R.drawable.button_round));
                        }
                    };
                    downTimer.start();
                    /*Timer buttonTimer = new Timer();
                    buttonTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnNext.setVisibility(View.VISIBLE);
                                    btnNext.setEnabled(true);
                                    btnNext.setBackground(getResources().getDrawable(R.drawable.button_round));
                                }
                            });
                        }
                    }, sec*1000);*/
                }
            }
        }
    }

    public void initViews() {
        tvBack = findViewById(R.id.tvBack);
        editMobileNo = findViewById(R.id.editMobileNo);
        btnNext = findViewById(R.id.btnNext);
        airtelLogo = findViewById(R.id.airtelLogo);
        tvCountdown = findViewById(R.id.tvCountdown);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) airtelLogo.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            airtelLogo.setLayoutParams(params);
        } else {
            // In portrait
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) airtelLogo.getLayoutParams();
            params.setMargins(0, 70, 0, 70);
            airtelLogo.setLayoutParams(params);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tvBack: {
                onBackPressed();
                break;
            }
            case R.id.btnNext: {
                mobile = editMobileNo.getText().toString().trim();
                md5(mobile + Constant.STATIC_KEY);
                key = hexString.toString();

                if (NetworkUtils.isNetworkConnected(ForgetPassword.this)) {

                    callForgetPwdApi(); //Calling reset password API if internet connected

                } else {
                    Toast.makeText(ForgetPassword.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void callForgetPwdApi() {


        if (mobile.isEmpty()) {
            Toast.makeText(this, "Field can not be empty!", Toast.LENGTH_SHORT).show();
        } else if (mobile.length() != 10) {
            Toast.makeText(this, "Enter a valid Mobile Number!", Toast.LENGTH_SHORT).show();
        } else {
            JSONObject mainJObject = new JSONObject();

            try {

                mainJObject.put("mobile", mobile);
            } catch (JSONException ignored) {
            }
            final String mRequestBody = mainJObject.toString();

            try {
                mDialog = UIUtils.showProgressDialog(ForgetPassword.this);
                mDialog.setCancelable(false);

                RequestQueue queue = Volley.newRequestQueue(this);
                String url = BuildConfig.baseUrl + getForgotPwdEndUrl;
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
                                        if (jsonObject != null) {

                                            String token = jsonObject.optString("token");

                                            if (!token.equalsIgnoreCase("")) {
                                                showCustomDialog(token); //If success then otp dialog opens
                                            }

                                            btnNext.setVisibility(View.VISIBLE);
                                            btnNext.setEnabled(false);
                                            btnNext.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
                                            MethodsUtil.setOTPCacheTime(ForgetPassword.this, Constant.OTP_CACHE_TIME);


                                            CountDownTimer downTimer = new CountDownTimer(Constant.OTP_CACHE_TIME * 1000, 1000) {
                                                @Override
                                                public void onTick(long millisUntilFinished) {
                                                    tvCountdown.setVisibility(View.VISIBLE);
                                                    tvCountdown.setText("Please wait for " + (millisUntilFinished+1000)/1000 + " seconds");;
                                                    //Toast.makeText(ForgetPassword.this, ""+millisUntilFinished/1000, Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onFinish() {
                                                    tvCountdown.setVisibility(View.GONE);
                                                    btnNext.setVisibility(View.VISIBLE);
                                                    btnNext.setEnabled(true);
                                                    btnNext.setBackground(getResources().getDrawable(R.drawable.button_round));
                                                }
                                            };
                                            downTimer.start();
                                            /*Timer buttonTimer = new Timer();
                                            buttonTimer.schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            btnNext.setVisibility(View.VISIBLE);
                                                            btnNext.setEnabled(true);
                                                            btnNext.setBackground(getResources().getDrawable(R.drawable.button_round));
                                                        }
                                                    });
                                                }
                                            }, Constant.OTP_CACHE_TIME*1000);*/
                                        }

                                    } else {
                                        Toast.makeText(ForgetPassword.this, msg, Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(ForgetPassword.this, msg, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ForgetPassword.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("key", key);
                        params.put("Content-Type", "application/json");

//                        String credentials = BuildConfig.basicAuthUsername + ":" + BuildConfig.basicAuthPass;
//                        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
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

                Toast.makeText(ForgetPassword.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Dialog to enter OTP
    public void showCustomDialog(String token) {
        final Dialog dialog = new Dialog(ForgetPassword.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.txt_title_dialog);
        title.setText("Verification Code");

        TextView msg = (TextView) dialog.findViewById(R.id.txt_msg_dialog);
        msg.setText("Please enter the Verification code sent to your registered mobile number.");

        TextInputEditText otpText = dialog.findViewById(R.id.inputText);
        otpText.setHint("Verification Code");
        otpText.setInputType(InputType.TYPE_CLASS_NUMBER);
        int maxLength = 4;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        otpText.setFilters(fArray);

        //For auto-reading OTP
        SmsReceiver.bindListener(new SmsListener() {
            @Override
            public void messageReceived(String messageText) {
                otpText.setText(messageText);
            }
        });

        Button positiveDialogButton = (Button) dialog.findViewById(R.id.btn_yes);
        positiveDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(otpText.getText().toString().trim())) {
                    dialog.show();
                    Toast.makeText(ForgetPassword.this, "Please enter Verification Code!", Toast.LENGTH_SHORT).show();
                } else {
                    if (otpText.length() == 4) {

                        if (NetworkUtils.isNetworkConnected(ForgetPassword.this)) {

                            dialog.show();
                            callOtpApi(otpText.getText().toString().trim(), token); //Calling otp API if internet connected

                        } else {
                            dialog.show();
                            Toast.makeText(ForgetPassword.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        dialog.show();
                        Toast.makeText(ForgetPassword.this, "Invalid Verification Code!", Toast.LENGTH_SHORT).show();
                    }
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

    private void callOtpApi(String otp, String token) {

        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("mobile", mobile);
            mainJObject.put("otp", otp);
            mainJObject.put("token", token);
        } catch (JSONException ignored) {
        }
        final String mRequestBody = mainJObject.toString();

        try {
            mDialog = UIUtils.showProgressDialog(ForgetPassword.this);
            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + getOtpVerifyEndUrl;
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
                                    if (jsonObject != null) {

                                        String token = jsonObject.optString("token");

                                        // Toast.makeText(ForgetPassword.this, msg, Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(ForgetPassword.this, ResetPassword.class);
                                        intent.putExtra("mobile", mobile);
                                        intent.putExtra("token", token);
                                        intent.putExtra("key", key);
                                        startActivity(intent);
                                        finish();
                                        ForgetPassword.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
                                    }

                                } else {
                                    Toast.makeText(ForgetPassword.this, msg, Toast.LENGTH_LONG).show();
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
                                Toast.makeText(ForgetPassword.this, msg, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ForgetPassword.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("key", key);
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

            Toast.makeText(ForgetPassword.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ForgetPassword.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
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
