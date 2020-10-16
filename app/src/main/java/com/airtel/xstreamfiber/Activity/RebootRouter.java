package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/*
    In Reboot Router, if reboot button is clecked then API is called to reboot the router.
 */
public class RebootRouter extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgBack;
    private Button btnReboot;
    private Dialog mDialog;
    private String reebootRouterEndUrl="v1/reboot";
    private TextView txtToolbarTitle;
    private String userSerial;
    private String apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reboot_router);

        txtToolbarTitle=(TextView)findViewById(R.id.txtToolbarTitle);
        txtToolbarTitle.setText("Reboot Router");
        imgBack=(ImageView)findViewById(R.id.imgBack);
        imgBack.setOnClickListener(this);
        btnReboot=(Button)findViewById(R.id.btnReboot);
        btnReboot.setOnClickListener(this);

        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);
        apiKey = dduSharedPref.getString("apiKey", "");

        if (!NetworkUtils.isNetworkConnected(RebootRouter.this)) {
            Toast.makeText(RebootRouter.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBack: {
                onBackPressed();
                break;
            }  case R.id.btnReboot: {
                //if internet connected then call API
                if (NetworkUtils.isNetworkConnected(RebootRouter.this)) {
                    if (MethodsUtil.isRebootCacheOver(getApplicationContext())){
                        rebootRouter();
                    }else {
                        Toast.makeText(this, "Please wait 3 minutes for the link to stabilize.", Toast.LENGTH_LONG).show();
                    }
                } else  {
                    Toast.makeText(RebootRouter.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void rebootRouter() {
        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial",userSerial);
        } catch (JSONException ignored) {
        }
        final String mRequestBody = mainJObject.toString();

        try {
            mDialog = UIUtils.showProgressDialog(RebootRouter.this);
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + reebootRouterEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,mainJObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            if(response!=null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                if(statusCode.equals("200")){
                                    MethodsUtil.setRebootCacheTime(getApplicationContext(), 3);
                                    Toast.makeText(RebootRouter.this, msg, Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(RebootRouter.this, msg, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(RebootRouter.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(RebootRouter.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(RebootRouter.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        RebootRouter.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }
}
