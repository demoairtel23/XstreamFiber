package com.airtel.xstreamfiber.Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.Util.MCrypt;
import com.airtel.xstreamfiber.Util.MethodsUtil;
import com.airtel.xstreamfiber.Util.NetworkUtils;
import com.airtel.xstreamfiber.Util.RootUtil;
import com.airtel.xstreamfiber.Util.UIUtils;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
    In Login, when user enters correct mobile number and password and hit login then Login API is called and user is allowed to enter the app.
    If Register is clicked then a dialog appears for informing user that he has to be connected to home network for registering, on
    clicking OK user is redirected to Registration screen.
*/
public class Login extends AppCompatActivity implements View.OnClickListener {

    private static final int REQ_CODE_VERSION_UPDATE = 123456;
    private String TAG = "Login";
    private String TAG3 = "Login3";
    private static StringBuilder hexString;
    private Button login;
    private Button stop;
    private Dialog mDialog;
    private TextView tvHowToRegister, tvForgetPwd;
    private Button btnLogin, signUpRegister;
    private TextInputEditText editMobileNo, editpass;
    //    private String loginEndUrl = "v1/login";
    private String loginEndUrl = "v2/login";
    private String howToRgister = "v2/how_to_register";
    private LinearLayout llImage;
    private ImageView airtelLogo;
    private String message = "Mobile device must be connected to the home network for the sign-up procedure to be completed successfully.";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    //    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 123;
//    private final static int REQUEST_READ_SMS_PERMISSION = 3004;
    private static final int LOCATION = 1;
    private TextView tvVersionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //temp to be removed in next release build
        //tempMethod();

        //Initializing all views
        setContentView(R.layout.activity_login);
        if (RootUtil.isDeviceRooted())
            showAlertDialogAndExitApp("This device is rooted. You can't use this app.");

        tvVersionName = findViewById(R.id.tvVersionName);
        tvVersionName.setText("Version : "+BuildConfig.VERSION_NAME);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        signUpRegister = findViewById(R.id.signUpRegister);
        signUpRegister.setOnClickListener(this);
        tvHowToRegister = findViewById(R.id.tvHowToRegister);
        tvHowToRegister.setOnClickListener(this);
        editMobileNo = findViewById(R.id.editMobileNo);
        editpass = findViewById(R.id.editpass);
        tvForgetPwd = findViewById(R.id.tvForgetPwd);
        tvForgetPwd.setOnClickListener(this);

        llImage = findViewById(R.id.llImage);
        airtelLogo = findViewById(R.id.airtelLogo);

        checkPermission();

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

        if (Constant.FLAG_SPLASH){
            Constant.FLAG_SPLASH = false;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    MethodsUtil.checkApkVersion(Login.this);
                }
            });
        }
    }

    public void showAlertDialogAndExitApp(String message) {

        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(Login.this, R.style.AlertDialogDanger)).create();
        alertDialog.setTitle("Alert");
        alertDialog.setCancelable(false);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MethodsUtil.minimizeApp(Login.this);
                        finish();
                    }
                });

        alertDialog.show();
    }

    //When clicked on Register showing dialog to inform user to connect to home network for registering
    public void showCustomDialog() {
        final Dialog dialog = new Dialog(Login.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.txt_title_dialog);
        title.setText("Information");

        TextView msg = (TextView) dialog.findViewById(R.id.txt_msg_dialog);
        msg.setText(message);
        // msg.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);

        TextInputEditText otpText = dialog.findViewById(R.id.inputText);
        otpText.setVisibility(View.GONE);

        Button positiveDialogButton = (Button) dialog.findViewById(R.id.btn_yes);
        positiveDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
                Login.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvHowToRegister: {
                //If internet connected hit the api
                if (NetworkUtils.isNetworkConnected(Login.this)) {
                    Intent intent = new Intent(Login.this, HowToRegister.class);
                    intent.putExtra("pageUrl", BuildConfig.baseUrl + howToRgister);
                    startActivity(intent);
                    Login.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
                } else {
                    Toast.makeText(Login.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.signUpRegister: {
//                showCustomDialog();
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
                Login.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
                break;
            }
            case R.id.btnLogin: {
                //If internet connected hit the api
                if (NetworkUtils.isNetworkConnected(Login.this)) {
                    loginApi();
                } else {
                    Toast.makeText(Login.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.tvForgetPwd: {
                startActivity(new Intent(Login.this, ForgetPassword.class));
                Login.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

                break;
            }
        }
    }

    private void testMethod() {
        RequestQueue queue = Volley.newRequestQueue(Login.this);
//        String url = "https://www.google.com/";
        String url = "http://checkip.amazonaws.com/";
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //UIUtils.dismissDialog(mDialog);
                        if (response != null) {
                            if (!response.equalsIgnoreCase("")){
//                                flag = true;
                            }
                            Toast.makeText(Login.this, "Response " + response.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            Toast.makeText(Login.this, "message " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        } else if (error instanceof AuthFailureError) {
                            Toast.makeText(Login.this, error.networkResponse.statusCode+" message " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(Login.this, error.networkResponse.statusCode+" message " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        } else if (error instanceof NetworkError) {
                            Toast.makeText(Login.this, error.networkResponse.statusCode+" message " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(Login.this, error.networkResponse.statusCode+" message " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }
        );
        getRequest.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(1),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );
        queue.add(getRequest);
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

    private void loginApi() {
        if (!TextUtils.isEmpty(editMobileNo.getText().toString().trim()) && !TextUtils.isEmpty(editpass.getText().toString().trim())) {
            String enteredMobile = editMobileNo.getText().toString().trim();
            String enteredpass = editpass.getText().toString().trim();
            if (enteredMobile.length() == 10) {
                if (!(enteredpass.length() > 0)) {
                    Toast.makeText(Login.this, "Enter Password", Toast.LENGTH_SHORT).show();
                }
                else {

                    JSONObject mainJObject = new JSONObject();

                    MCrypt mcrypt = new MCrypt();
                    /* Encrypt */
                    String encryptedPass = null;
                    try {
                        encryptedPass = MCrypt.bytesToHex(mcrypt.encrypt(enteredpass)); //https://github.com/serpro/Android-PHP-Encrypt-Decrypt

                    } catch (Exception ignored) {
                    }


                    try {
                        mainJObject.put("mobile", enteredMobile);
                        mainJObject.put("password", encryptedPass);
                    } catch (JSONException ignored) {
                    }
                    final String mRequestBody = mainJObject.toString();

                    try {
                        mDialog = UIUtils.showProgressDialog(Login.this);
                        mDialog.setCancelable(false);

                        RequestQueue queue = Volley.newRequestQueue(this);
                        String url = BuildConfig.baseUrl + loginEndUrl;
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
                                                    String userSerial = jsonObject.optString("serial");

                                                    md5(userSerial + Constant.STATIC_KEY);
                                                    String apiKey = hexString.toString();
                                                    //Log.d("keyApi", apiKey);


                                                    SharedPreferences loginPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
                                                    String encryptedUserSerial = MethodsUtil.encryptSerial(getApplicationContext(), userSerial);
                                                    loginPref.edit().putString("userSerial", encryptedUserSerial).apply();
                                                    loginPref.edit().putString("apiKey", apiKey).apply();

                                                    // callUpgradeApi();

                                                    //set Login Session Time Out
                                                    int logoutTime = response.optInt("logout_time");
                                                    if (logoutTime > 0)
                                                        MethodsUtil.setLoginSessionTime(Login.this, logoutTime);

                                                    Intent intent = new Intent(Login.this, MainMenuActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                    Login.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
                                                }
                                                // Toast.makeText(Login.this, msg, Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(Login.this, msg, Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(Login.this, msg, Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(Login.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                        ) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("Content-Type", "application/json");

//                                String credentials = BuildConfig.basicAuthUsername + ":" + BuildConfig.basicAuthPass;
//                                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                                String auth = MethodsUtil.getAuth();
                                params.put("Authorization", auth);

                                return params;
                            }

                        };
                        /*getRequest.setRetryPolicy(new DefaultRetryPolicy(
                                (int) TimeUnit.SECONDS.toMillis(30),
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                        );*/

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
                        Toast.makeText(Login.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }

                }
            } else {
                Toast.makeText(Login.this, "Invalid Mobile Number", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(Login.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
        }

    }

    public static final String md5(final String s) {
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

    //--------------------------------------CHECK PERMISSION FOR THE USER--------------------------------------------------------
    private void checkPermission() {


        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                + ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                + ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_READ_SMS);

            //  permission = false;
        } else {
            // permission = true;
        }*/

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                Toast.makeText(this, "Please grant permission", Toast.LENGTH_SHORT).show();
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
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
            /*case MY_PERMISSIONS_REQUEST_READ_SMS: {
                // If request is cancelled, the result array is empty.


                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.RECEIVE_SMS) + ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        // tryToAutoReadOTP();

                        //permission = true;
                    }
                }
                break;
            }*/
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

    @Override
    public void onBackPressed() {
        MethodsUtil.minimizeApp(Login.this);
        finish();
    }

    private void tempMethod() {
        SharedPreferences sharedPreferences = getSharedPreferences("temp", MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("tempdata", false)) {
            sharedPreferences.edit().putBoolean("tempdata", true).apply();
            SharedPreferences userPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
            userPref.edit().clear().apply();
        }
    }
}
