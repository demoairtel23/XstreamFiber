package com.airtel.xstreamfiber.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airtel.xstreamfiber.Apphelper.SmsListener;
import com.airtel.xstreamfiber.Apphelper.SmsReceiver;
import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.Util.MCrypt;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/*
    In Registration, firstly wifi connectivity is checked if user is connected to wifi then its ip address is fetched
    and all other information entered by user is posted in api body including io address and on success otp dialog appears
    which auto reads the otp or self enter, and on submiting OTP api is called to verify the user and on success user is redirected
    to login screen.
 */
public class Registration extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgBack;
    private TextView txtToolbarTitle, regLogin, tvCountdown;
    private Button btnRegister;
    private TextInputEditText mobileNum, pwd, confirmPwd, editEmail;
    private String mobile, password, confirmPassword, email, ip, otp;
    private Dialog mDialog;
    //    private String setRegistrationEndUrl = "v1/register";
    private String setRegistrationEndUrl = "v2/register";
//    private String setOtpEndUrl = "v1/register/otp";
    private String setOtpEndUrl = "v2/register/otp";
    private AlertDialog alert;
    private AlertDialog.Builder alert1;
    private Dialog customDialog;
    DatagramSocket socket;
    boolean permission;
    private static StringBuilder hexString;
//    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);
        txtToolbarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
        txtToolbarTitle.setText("Registration");
        imgBack = (ImageView) findViewById(R.id.imgBack);
        regLogin = findViewById(R.id.regLogin);
        btnRegister = findViewById(R.id.btnRegister);

        mobileNum = findViewById(R.id.mobileNum);
        pwd = findViewById(R.id.pwd);
        confirmPwd = findViewById(R.id.confirmPwd);
        editEmail = findViewById(R.id.editEmail);
        tvCountdown = findViewById(R.id.tvCountdown);

        imgBack.setOnClickListener(this);
        regLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);


        if (!MethodsUtil.isRegOTPCacheOver(getApplicationContext())) {
            btnRegister.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
            btnRegister.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));

            SharedPreferences sharedPreferences = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
            if (sharedPreferences.getLong("reg_otp_cache_time", 0) != 0) {
                if (Calendar.getInstance().getTime().getTime() < (sharedPreferences.getLong("reg_otp_cache_time", 0))) {
                    //reboot cache time is over
                    long t = sharedPreferences.getLong("reg_otp_cache_time", 0) - Calendar.getInstance().getTime().getTime();
                    Calendar rem = Calendar.getInstance();
                    rem.setTimeInMillis(t);
                    int sec = rem.get(Calendar.SECOND);


                    CountDownTimer downTimer = new CountDownTimer(sec * 1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            tvCountdown.setVisibility(View.VISIBLE);
                            tvCountdown.setText("Please wait for " + (millisUntilFinished+1000)/1000 + " seconds");
                        }

                        @Override
                        public void onFinish() {
                            tvCountdown.setVisibility(View.GONE);
                            btnRegister.setVisibility(View.VISIBLE);
                            btnRegister.setEnabled(true);
                            btnRegister.setBackground(getResources().getDrawable(R.drawable.button_round));
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
                                    btnRegister.setVisibility(View.VISIBLE);
                                    btnRegister.setEnabled(true);
                                    btnRegister.setBackground(getResources().getDrawable(R.drawable.button_round));
                                }
                            });
                        }
                    }, sec * 1000);*/
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBack: {
                onBackPressed();
                break;
            }
            case R.id.regLogin: {
                startActivity(new Intent(Registration.this, Login.class));
                Registration.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
                finish();
                break;
            }
            case R.id.btnRegister: {

                if (NetworkUtils.isNetworkConnected(Registration.this)) {
                    getIpAddress();
                } else {
                    Toast.makeText(Registration.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /*public void tryToAutoReadOTP() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                + ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS},
                    MY_PERMISSIONS_REQUEST_READ_SMS);

            permission = false;
        } else {
            permission = true;
        }
    }*/

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_SMS: {
                // If request is cancelled, the result array is empty.


                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                            + ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        tryToAutoReadOTP();

                        permission = true;
                    }
                }
                break;
            }
        }
    }*/

    //Dialog to enter OTP
    public void showCustomDialog(String token) {
        final Dialog dialog = new Dialog(Registration.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.txt_title_dialog);
        title.setText("OTP Verification");

        TextView msg = (TextView) dialog.findViewById(R.id.txt_msg_dialog);
        msg.setText("Please enter the OTP sent to your registered mobile number.");

        TextInputEditText otpText = dialog.findViewById(R.id.inputText);
        otpText.setHint("Enter OTP");
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
                    Toast.makeText(Registration.this, "Please enter OTP.", Toast.LENGTH_SHORT).show();
                } else {
                    if (otpText.length() == 4) {

                        if (NetworkUtils.isNetworkConnected(Registration.this)) {

                            dialog.show();
                            callOtpApi(otpText.getText().toString().trim(), token); //Calling otp API if internet connected

                        } else {
                            Toast.makeText(Registration.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        dialog.show();
                        Toast.makeText(Registration.this, "Invalid OTP!", Toast.LENGTH_SHORT).show();
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

    //OTP api call
    private void callOtpApi(String otp, String token) {
        mobile = mobileNum.getText().toString().trim();

        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("mobile", mobile);
            mainJObject.put("otp", otp);
            mainJObject.put("token", token);
        } catch (JSONException ignored) {
        }

        try {
            mDialog = UIUtils.showProgressDialog(Registration.this);
            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + setOtpEndUrl;
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

                                        // Toast.makeText(Registration.this, msg, Toast.LENGTH_LONG).show();

                                        String userSerial = jsonObject.optString("serial");

                                        md5(userSerial + Constant.STATIC_KEY);
                                        String apiKey = hexString.toString();

                                        SharedPreferences loginPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
                                        String encryptedUserSerial = MethodsUtil.encryptSerial(getApplicationContext(), userSerial);
                                        loginPref.edit().putString("userSerial", encryptedUserSerial).apply();
                                        loginPref.edit().putString("apiKey", apiKey).apply();

                                        //set Login Session Time Out
                                        int logoutTime = response.optInt("logout_time");
                                        if (logoutTime>0)
                                            MethodsUtil.setLoginSessionTime(Registration.this, logoutTime);

                                        Intent intent = new Intent(Registration.this, MainMenuActivity.class);
                                        startActivity(intent);//open Login screen on successful registration with otp
                                        finish();
                                        Registration.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
                                    }
                                } else {
                                    Toast.makeText(Registration.this, msg, Toast.LENGTH_LONG).show();
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
                                Toast.makeText(Registration.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(Registration.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
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
            Toast.makeText(Registration.this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
    }

    public void getIpAddress() {
        mobile = mobileNum.getText().toString().trim();
        password = pwd.getText().toString().trim();
        confirmPassword = confirmPwd.getText().toString().trim();
        email = editEmail.getText().toString().trim();

        if (TextUtils.isEmpty(mobileNum.getText().toString().trim()) || TextUtils.isEmpty(pwd.getText().toString().trim()) || TextUtils.isEmpty(confirmPwd.getText().toString().trim()) || TextUtils.isEmpty(editEmail.getText().toString().trim())) {
            Toast.makeText(this, "Fields can not be empty!", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPassword)) {
            //confirmPwd.setError("Passwords does not match!!");
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(Registration.this, "Invalid Email Address!", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 4) {
            Toast.makeText(Registration.this, "Password must contain atleast 4 characters!", Toast.LENGTH_SHORT).show();
        } else if (mobile.length() != 10) {
            Toast.makeText(Registration.this, "Invalid Mobile Number!", Toast.LENGTH_SHORT).show();
        } else {
            //For getting private ip address of the Wifi router, device is connected with
            ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            /*NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
            for (NetworkInfo netInfo : networkInfo) {
                //for checking if the user is connected to wifi or not
                if (netInfo.getTypeName().equalsIgnoreCase("WIFI")) {
                    if (netInfo.isConnected()) {*/

                        RequestQueue queue2 = Volley.newRequestQueue(Registration.this);
                        String urlip = "http://checkip.amazonaws.com/";

                        mDialog = UIUtils.showProgressDialog(Registration.this);
                        mDialog.setCancelable(false);

                        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlip, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                ip = response;
                                Matcher ipMatcher = Patterns.IP_ADDRESS.matcher(response.trim());
                                if (ipMatcher.matches()) {
                                    callRegistrationApi(); //If got the IP address then call registration API
                                } else {
                                    UIUtils.dismissDialog(mDialog);
                                    Toast.makeText(Registration.this, "Seems some connectivity issue.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                UIUtils.dismissDialog(mDialog);
                                if (error.networkResponse!=null){
                                    String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                    Toast.makeText(Registration.this, msg, Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(Registration.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        queue2.add(stringRequest);

                    /*} else {
                        Toast.makeText(this, "Please connect to home WiFi to proceed.", Toast.LENGTH_SHORT).show();
                    }
                }
            }*/
        }
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

    private void callRegistrationApi() {

        JSONObject mainJObject = new JSONObject();

        MCrypt mcrypt = new MCrypt();
        /* Encrypt */
        String encryptedPass = null;
        try {
            encryptedPass = MCrypt.bytesToHex(mcrypt.encrypt(password)); //https://github.com/serpro/Android-PHP-Encrypt-Decrypt

        } catch (Exception ignored) {
        }

        try {

            mainJObject.put("mobile", mobile);
            mainJObject.put("password", encryptedPass);
            mainJObject.put("email", email);
            mainJObject.put("ip", ip);
        } catch (JSONException ignored) {
        }
        final String mRequestBody = mainJObject.toString();

        try {
//            mDialog = UIUtils.showProgressDialog(Registration.this);
//            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + setRegistrationEndUrl;
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

                                        if (!token.equalsIgnoreCase(""))
                                        {
                                            showCustomDialog(token); //If success then otp dialog opens
                                        }

                                        btnRegister.setVisibility(View.VISIBLE);
                                        btnRegister.setEnabled(false);
                                        btnRegister.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
                                        MethodsUtil.setRegOTPCacheTime(Registration.this, Constant.REG_OTP_CACHE_TIME);


                                        CountDownTimer downTimer = new CountDownTimer(Constant.REG_OTP_CACHE_TIME * 1000, 1000) {
                                            @Override
                                            public void onTick(long millisUntilFinished) {
                                                tvCountdown.setVisibility(View.VISIBLE);
                                                tvCountdown.setText("Please wait for " + (millisUntilFinished+1000)/1000 + " seconds");
                                            }

                                            @Override
                                            public void onFinish() {
                                                tvCountdown.setVisibility(View.GONE);
                                                btnRegister.setVisibility(View.VISIBLE);
                                                btnRegister.setEnabled(true);
                                                btnRegister.setBackground(getResources().getDrawable(R.drawable.button_round));
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
                                                        btnRegister.setVisibility(View.VISIBLE);
                                                        btnRegister.setEnabled(true);
                                                        btnRegister.setBackground(getResources().getDrawable(R.drawable.button_round));
                                                    }
                                                });
                                            }
                                        }, Constant.REG_OTP_CACHE_TIME * 1000);*/
                                    }

                                } else {
                                    UIUtils.dismissDialog(mDialog);
                                    Toast.makeText(Registration.this, msg, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(Registration.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(Registration.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
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
        } catch (Exception e) { // for caught any exception during the excecution of the service

            UIUtils.dismissDialog(mDialog);
            Toast.makeText(Registration.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Registration.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }
}
