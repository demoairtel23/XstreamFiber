package com.airtel.xstreamfiber.Fragment;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.Util.MethodsUtil;
import com.airtel.xstreamfiber.Util.NetworkUtils;
import com.airtel.xstreamfiber.Util.UIUtils;
import com.airtel.xstreamfiber.model.BlockedDevice;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class ParentalControlBlockedDevice extends Fragment {

    private TextView tvTitle, tv_empty;
    private RecyclerView rvBlockedDevices;
    private Button btnUnblockedDevicces;
    private Dialog mDialog;
    private AppCompatActivity mContext;
    private String blockUnblockEndUrl = "v1/mac/block";
    private String parentalControlBlockedEndUrl = "v1/block_devices";
    private com.airtel.xstreamfiber.Adapters.ParentalControlBlockedDevices parentalControlBlockedDevicesAdapter;
    ArrayList<BlockedDevice> arrayListBlock;
    BlockedDevice blockedDevice;
    private String userSerial;
    private String apiKey;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public ParentalControlBlockedDevice(AppCompatActivity mContext) {
        this.mContext = mContext;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {




        SharedPreferences loginharedPref = getActivity().getSharedPreferences(Constant.LOGIN_PREF,MODE_PRIVATE);
//        userSerial = loginharedPref.getString("userSerial", "");
        String userSerialEncrypted = loginharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getActivity(), userSerialEncrypted);
        apiKey = loginharedPref.getString("apiKey", "");

        sharedPreferences = mContext.getSharedPreferences(userSerial, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (NetworkUtils.isNetworkConnected(mContext)) {

            parentalControlApiCall();

        } else  {

            Toast.makeText(mContext, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
        }

        return inflater.inflate(R.layout.fragment_parental_control_blocked_devices, container, false);

    }

    //Fetching data from the server
    public void parentalControlApiCall()
    {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serial", userSerial);
        }
        catch (Exception ignored)
        {
        }
        try {
            mDialog = UIUtils.showProgressDialog(getActivity());
            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            String url = BuildConfig.baseUrl + parentalControlBlockedEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);

                            if(response!=null) {

                                tv_empty.setVisibility(View.GONE);

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");

                                arrayListBlock = new ArrayList<>();
                                if(statusCode.equals("200")){

                                    tv_empty.setVisibility(View.GONE);

                                    try {

                                        JSONArray jsonArray = response.optJSONArray("data");

                                        for(int i=0;i<jsonArray.length();i++){
                                            blockedDevice = new BlockedDevice();
                                            JSONObject jsonObject1=jsonArray.optJSONObject(i);
                                            String macAddress=jsonObject1.optString("mac_address");
                                            String hostName =jsonObject1.optString("device_name");
                                            String permanentDisabled = jsonObject1.optString("permanent_disabled");
                                            String timeLimit = jsonObject1.optString("time_limit");
                                            String startTime = jsonObject1.optString("start_time");
                                            String endTime = jsonObject1.optString("end_time");

                                            blockedDevice.setMac_address(macAddress);
                                            blockedDevice.setDevice_name(hostName);
                                            blockedDevice.setPermanent_disabled(permanentDisabled);
                                            blockedDevice.setTime_limit(timeLimit);
                                            blockedDevice.setStart_time(startTime);
                                            blockedDevice.setEnd_time(endTime);

                                            arrayListBlock.add(blockedDevice);




                                        }

                                        if (jsonArray.length() == 0)
                                        {
                                            tv_empty.setVisibility(View.VISIBLE);
                                        }
                                        else
                                        {
                                            tv_empty.setVisibility(View.GONE);
                                        }
                                        if (parentalControlBlockedDevicesAdapter != null)
                                        {
                                            parentalControlBlockedDevicesAdapter.notifyDataSetChanged();
                                        }
                                        else
                                        {
                                            setAdapter();
                                        }



                                    } catch (Exception e) {
                                        Toast.makeText(getActivity(), "Error: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }else {
                                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                                }

                            }
                            else
                                tv_empty.setVisibility(View.VISIBLE);


                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            UIUtils.dismissDialog(mDialog);
                            if (error.networkResponse!=null){
                                String msg = MethodsUtil.getStatusCodeMessage(error.networkResponse.data);
                                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(getActivity(), R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tv_header);
        tv_empty = view.findViewById(R.id.tv_empty);
        btnUnblockedDevicces = view.findViewById(R.id.btn_unblocked_devices);
        rvBlockedDevices = view.findViewById(R.id.rv_blocked_devices);
        rvBlockedDevices.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    private void setAdapter() {

        parentalControlBlockedDevicesAdapter = new com.airtel.xstreamfiber.Adapters.ParentalControlBlockedDevices(getContext(), arrayListBlock, ParentalControlBlockedDevice.this);
        rvBlockedDevices.setAdapter(parentalControlBlockedDevicesAdapter);
    }

    //Unblocking blocked devices
    public void blockUnblock(int position, String blockedStatus)
    {
        String macAddress = arrayListBlock.get(position).getMac_address();
        String hostName = arrayListBlock.get(position).getDevice_name();
        String permanentDisabled = arrayListBlock.get(position).getPermanent_disabled();
        String timeLimit = arrayListBlock.get(position).getTime_limit();
        String startTime = arrayListBlock.get(position).getStart_time();
        String endTime = arrayListBlock.get(position).getEnd_time();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serial", userSerial);
            jsonObject.put("mac_address",macAddress);
            jsonObject.put("device_name",hostName);
            jsonObject.put("start_time", startTime);
            jsonObject.put("end_time", endTime);
            jsonObject.put("permanent_disabled", permanentDisabled);
            jsonObject.put("time_limit", timeLimit);
            jsonObject.put("blocked_status", blockedStatus);

        }
        catch (Exception ignored)
        {
        }
        try {
            mDialog = UIUtils.showProgressDialog(mContext);
            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(mContext);
            String url = BuildConfig.baseUrl + blockUnblockEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);

                            // UnblockedDevice data = new UnblockedDevice();

                            if(response!=null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                if(statusCode.equals("200"))
                                {
                                    arrayListBlock.remove(position);            //Removing data from the list of devices if successfully unblocked
                                    parentalControlBlockedDevicesAdapter.notifyDataSetChanged();
                                    rvBlockedDevices.setAdapter(parentalControlBlockedDevicesAdapter);
                                    editor.putString("Tab", "Tab3").apply();

                                   // parentalControlBlockedDevicesAdapter.notifyDataSetChanged();
                                   // parentalControlApiCall();
                                 //   Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(mContext, R.string.error_generic, Toast.LENGTH_SHORT).show();
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

                }
            });
            queue.add(getRequest);

        } catch (Exception e) { // for caught any exception during the excecution of the service

            UIUtils.dismissDialog(mDialog);
            Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }
}
