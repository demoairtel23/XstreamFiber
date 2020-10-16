package com.airtel.xstreamfiber.Fragment;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
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
import com.airtel.xstreamfiber.model.UnblockedDevice;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class ParentalControlUnblockedInactiveDevices extends Fragment {

    private PopupWindow popupWindow;
    private TextView tvTitle, tv_empty;
    private Button btnBlockedDevice;
    private RecyclerView rvUnblockedDevices;
    private Dialog mDialog;
    private AppCompatActivity mContext;
    private String parentalControlEndUrl = "v1/all_devices";
    private String blockUnblockEndUrl = "v1/mac/block";
    private com.airtel.xstreamfiber.Adapters.ParentalControl parentalControlAdapter;
    ArrayList<UnblockedDevice> arrayListUnblock, arrayListUnblockInactive ;
    private String userSerial;
    private String apiKey;
    private ArrayList<UnblockedDevice> arraylist ;
    ArrayList<UnblockedDevice> unblockedDeviceArrayList;
    Calendar repeatTime, currentTime;
    int repeat_after_min = 1;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean blocked = false;

    public ParentalControlUnblockedInactiveDevices(AppCompatActivity mContext) {
        this.mContext = mContext;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.fragment_parental_control, container, false);
        tv_empty = view.findViewById(R.id.tv_empty);
        rvUnblockedDevices = view.findViewById(R.id.rv_unblocked_devices);
        rvUnblockedDevices.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        SharedPreferences loginharedPref = getActivity().getSharedPreferences(Constant.LOGIN_PREF,MODE_PRIVATE);
//        userSerial = loginharedPref.getString("userSerial", "");
        String userSerialEncrypted = loginharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getActivity(), userSerialEncrypted);
        apiKey = loginharedPref.getString("apiKey", "");

        sharedPreferences = mContext.getSharedPreferences(userSerial, MODE_PRIVATE);
        editor = sharedPreferences.edit();


        if (sharedPreferences.getString("Tab","").equals("Tab3"))
        {
            if (NetworkUtils.isNetworkConnected(mContext)) {

                parentalControlApiCall();

            } else  {

                Toast.makeText(mContext, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            if (sharedPreferences.getLong("repeat_time_parental",0) != 0)
            {

                if (Calendar.getInstance().getTime().getTime() > (sharedPreferences.getLong("repeat_time_parental",0)))
                {

                    //Toast.makeText(mContext, "if call api", Toast.LENGTH_SHORT).show();
                    if (NetworkUtils.isNetworkConnected(mContext)) {

                        // Toast.makeText(mContext, "iif api called", Toast.LENGTH_SHORT).show();
                        parentalControlApiCall();

                    } else  {

                        Toast.makeText(mContext, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    setAdapter();
                }
            }
            else {

                if (NetworkUtils.isNetworkConnected(mContext)) {

                    parentalControlApiCall();

                } else {

                    Toast.makeText(mContext, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        }

        editor.putString("Tab", "tab2").apply();

        return view;
    }

    //Fetching data from the server
    public void parentalControlApiCall()
    {

       // Toast.makeText(mContext, "api call entry", Toast.LENGTH_SHORT).show();
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
            String url = BuildConfig.baseUrl + parentalControlEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url,jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);

                            if(response!=null) {

                                tv_empty.setVisibility(View.GONE);

                                arrayListUnblock = new ArrayList<>();
                                arrayListUnblockInactive = new ArrayList<>();

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                if(statusCode.equals("200")){

                                    currentTime = Calendar.getInstance();
                                    repeatTime = Calendar.getInstance();

                                    repeatTime.set(Calendar.HOUR, currentTime.get(Calendar.HOUR));
                                    repeatTime.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE) + repeat_after_min);
                                    if (currentTime.get(Calendar.MINUTE) + repeat_after_min >= 60)
                                    {
                                        repeatTime.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE) + repeat_after_min - 60);
                                        repeatTime.set(Calendar.HOUR, currentTime.get(Calendar.HOUR) + 1);
                                    }
                                    long repeat_time = repeatTime.getTime().getTime();
                                    editor.putLong("repeat_time_parental", repeat_time).apply();

                                    try {

                                        JSONObject jobject1 = response.optJSONObject("data");

                                        JSONObject jsonObjectActive = jobject1.optJSONObject("active");

                                        JSONObject jsonObjectInactive = jobject1.optJSONObject("Inactive");

                                        if(jsonObjectActive != null) {

                                            //tv_empty.setVisibility(View.GONE);

                                            Iterator<String> keyIterator = jsonObjectActive.keys();
                                            while (keyIterator.hasNext()) {

                                                String key = keyIterator.next();
                                                JSONObject activeJObject = jsonObjectActive.optJSONObject(key);
                                                String signalStrength = activeJObject.optString("SignalStrength");
                                                String active = activeJObject.optString("Active");
                                                String HostName = activeJObject.optString("HostName");
                                                String InterfaceType = activeJObject.optString("InterfaceType");
                                                String MACAddress = activeJObject.optString("MACAddress");

                                                UnblockedDevice unblockedDevice = new UnblockedDevice(signalStrength, active, HostName, InterfaceType, MACAddress);
                                                unblockedDevice.setSignalStrength(signalStrength);
                                                unblockedDevice.setActive(active);
                                                unblockedDevice.setHostName(HostName);
                                                unblockedDevice.setInterfaceType(InterfaceType);
                                                unblockedDevice.setMacAddress(MACAddress);

                                                arrayListUnblock.add(unblockedDevice);
                                            }
                                        }

                                        if(jsonObjectInactive != null) {

                                            tv_empty.setVisibility(View.GONE);

                                            Iterator<String> keyIterator1 = jsonObjectInactive.keys();
                                            while (keyIterator1.hasNext()) {
                                                String key = keyIterator1.next();
                                                JSONObject inactiveJObject = jsonObjectInactive.optJSONObject(key);
                                                String signalStrength = inactiveJObject.optString("SignalStrength");
                                                String active = inactiveJObject.optString("Active");
                                                String HostName = inactiveJObject.optString("HostName");
                                                String InterfaceType = inactiveJObject.optString("InterfaceType");
                                                String MACAddress = inactiveJObject.optString("MACAddress");

                                                UnblockedDevice unblockedDevice = new UnblockedDevice(signalStrength, active, HostName, InterfaceType, MACAddress);
                                                unblockedDevice.setSignalStrength(signalStrength);
                                                unblockedDevice.setActive(active);
                                                unblockedDevice.setHostName(HostName);
                                                unblockedDevice.setInterfaceType(InterfaceType);
                                                unblockedDevice.setMacAddress(MACAddress);

                                                arrayListUnblockInactive.add(unblockedDevice);

                                            }

                                            Gson gson = new Gson();
                                            String activeUnblock = gson.toJson(arrayListUnblock);
                                            editor.putString("arrayListUnblock", activeUnblock ).apply();
                                            String inactiveUnblock = gson.toJson(arrayListUnblockInactive);
                                            editor.putString("arrayListUnblockInactive", inactiveUnblock ).apply();

                                            if (parentalControlAdapter != null) {
                                                parentalControlAdapter.notifyDataSetChanged();
                                            } else {
                                                setAdapter();
                                            }

                                        }
                                        else
                                        {
                                            tv_empty.setText("There are no disconnected devices");
                                            tv_empty.setVisibility(View.VISIBLE);
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(getActivity(),
                                                "Error: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }else {
                                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
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

//        if (unblockedDeviceArrayList != null)
//        {
//            Log.e("UnblockDeviceInactive", "arrayListUnblock not null");
//            parentalControlAdapter = new com.airtel.xstreamfiber.Adapters.ParentalControl(getContext(), unblockedDeviceArrayList, ParentalControlUnblockedInactiveDevices.this);
//            rvUnblockedDevices.setAdapter(parentalControlAdapter);
//        }
//
//        if (arraylist != null)
//        {
//            Log.e("UnblockDeviceInactive", "arrayList not null");
//            parentalControlAdapter = new com.airtel.xstreamfiber.Adapters.ParentalControl(getContext(), arraylist, ParentalControlUnblockedInactiveDevices.this);
//            rvUnblockedDevices.setAdapter(parentalControlAdapter);
//        }
//        else {
//            Log.e("UnblockDeviceInactive", "arrayList null");
//            if (NetworkUtils.isNetworkConnected(mContext)) {
//
//                parentalControlApiCall();
//
//            } else  {
//
//                Toast.makeText(mContext, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tv_header);
      //  tv_empty = view.findViewById(R.id.tv_empty);
        btnBlockedDevice = view.findViewById(R.id.btn_blocked_devices);
//        rvUnblockedDevices = view.findViewById(R.id.rv_unblocked_devices);
//        rvUnblockedDevices.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    private void setAdapter() {



  //      UIUtils.dismissDialog(mDialog);

        Gson gson = new Gson();
        String inactiveUnblock = sharedPreferences.getString("arrayListUnblockInactive", "");
        Type type1 = new TypeToken<List<UnblockedDevice>>() {
        }.getType();
        arrayListUnblockInactive = gson.fromJson(inactiveUnblock, type1);

        if (arrayListUnblockInactive.size() == 0)
        {
            tv_empty.setText("There are no disconnected devices");
            tv_empty.setVisibility(View.VISIBLE);
        }



        parentalControlAdapter = new com.airtel.xstreamfiber.Adapters.ParentalControl(getContext(), arrayListUnblockInactive, ParentalControlUnblockedInactiveDevices.this);
        rvUnblockedDevices.setAdapter(parentalControlAdapter);
    }

    //Blocking devices
    public void blockUnblock(int position, String blockedStatus, String time_limit, int start_time, int end_time, String permanent_disabled)
    {

        String macAddress = arrayListUnblockInactive.get(position).getMacAddress();
        String hostName = arrayListUnblockInactive.get(position).getHostName();
        String startTime = "" + start_time;
        String endTime = "" + end_time;

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serial", userSerial);
            jsonObject.put("mac_address",macAddress);
            jsonObject.put("device_name",hostName);
            jsonObject.put("blocked_status", blockedStatus);
            jsonObject.put("start_time", startTime);
            jsonObject.put("end_time", endTime);
            jsonObject.put("permanent_disabled", permanent_disabled);
            jsonObject.put("time_limit", time_limit);

        }
        catch (Exception ignored)
        {
        }

        try {
            mDialog = UIUtils.showProgressDialog(mContext);
            // mDialog.setCancelable(true);

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
                                   // blocked = true;
                                    arrayListUnblockInactive.remove(position);        //Removing data from the list of devices if successfully blocked
                                    Gson gson = new Gson();
                                    String inactiveUnblock = gson.toJson(arrayListUnblockInactive);
                                    editor.putString("arrayListUnblockInactive", inactiveUnblock ).apply();
                                    parentalControlAdapter.notifyDataSetChanged();
                                    rvUnblockedDevices.setAdapter(parentalControlAdapter);
                                   // Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }
}