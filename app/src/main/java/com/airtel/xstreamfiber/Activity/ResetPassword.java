package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;
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
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ResetPassword extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "ResetPassword" ;
    private TextView tvBackToLogin;
    private ImageView airtelLogo;
    private TextInputEditText editPassword, editReEnterPassword;
    private Button btnResetPwd;
    private Dialog mDialog;
//    private String getResetPwdEndUrl = "v1/reset_password";
    private String getResetPwdEndUrl = "v2/reset_password";
    private String mobile, token, key, pwd, reEnterPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        
        initViews();
        
        tvBackToLogin.setOnClickListener(this);
        btnResetPwd.setOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mobile = bundle.getString("mobile");
            token = bundle.getString("token");
            key = bundle.getString("key");
        }


        //Managing UI in case of orientation change
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)airtelLogo.getLayoutParams();
            params.setMargins(0, 0,0,0);
            airtelLogo.setLayoutParams(params);
        } else {
            // In portrait
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)airtelLogo.getLayoutParams();
            params.setMargins(0, 70,0,70);
            airtelLogo.setLayoutParams(params);
        }
        
    }

    public void initViews()
    {
        tvBackToLogin = findViewById(R.id.tvBacktoLogin);
        editPassword = findViewById(R.id.editPassword);
        editReEnterPassword = findViewById(R.id.editReEnterPassword);
        btnResetPwd = findViewById(R.id.btnResetPwd);
        airtelLogo = findViewById(R.id.airtelLogo);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)airtelLogo.getLayoutParams();
            params.setMargins(0, 0,0,0);
            airtelLogo.setLayoutParams(params);
        } else {
            // In portrait
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)airtelLogo.getLayoutParams();
            params.setMargins(0, 70,0,70);
            airtelLogo.setLayoutParams(params);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.tvBacktoLogin:
            {
                startActivity(new Intent(ResetPassword.this, Login.class));
                finish();
                ResetPassword.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
                break;
            }
            case R.id.btnResetPwd:
            {
                pwd = editPassword.getText().toString().trim();
                reEnterPwd = editReEnterPassword.getText().toString().trim();

                if (pwd.isEmpty() || reEnterPwd.isEmpty())
                {
                    Toast.makeText(this, "Fields can not be empty!", Toast.LENGTH_SHORT).show();
                }
                else if (pwd.length() < 4 || reEnterPwd.length() < 4 )
                {
                    Toast.makeText(this, "Password must contain atleast 4 characters!", Toast.LENGTH_SHORT).show();
                }
                else if (!pwd.equals(reEnterPwd))
                {
                    Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (NetworkUtils.isNetworkConnected(ResetPassword.this)) {

                        callResetPwdApi(); //Calling reset password API if internet connected

                    } else {
                        Toast.makeText(ResetPassword.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }
    }

    private void callResetPwdApi() {

        JSONObject mainJObject = new JSONObject();

        MCrypt mcrypt = new MCrypt();
        /* Encrypt */
        String encryptedPass = null;
        try {
            encryptedPass = MCrypt.bytesToHex( mcrypt.encrypt(pwd) ); //https://github.com/serpro/Android-PHP-Encrypt-Decrypt

        } catch (Exception ignored) {
        }

        try {

            mainJObject.put("mobile", mobile);
            mainJObject.put("token", token);
            mainJObject.put("password", encryptedPass);
        } catch (JSONException ignored) {
        }
        final String mRequestBody = mainJObject.toString();

        try {
            mDialog = UIUtils.showProgressDialog(ResetPassword.this);
            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + getResetPwdEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,mainJObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            if(response!=null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                if(statusCode.equals("200")){

                                   // Toast.makeText(ResetPassword.this, msg, Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(ResetPassword.this, Login.class));
                                    finish();
                                    ResetPassword.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);

                                }else {
                                    Toast.makeText(ResetPassword.this, msg, Toast.LENGTH_LONG).show();
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
                                Toast.makeText(ResetPassword.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(ResetPassword.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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

            Toast.makeText(ResetPassword.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }
}
