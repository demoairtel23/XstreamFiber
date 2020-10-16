package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    In Registration, firstly wifi connectivity is checked if user is connected to wifi then its ip address is fetched
    and all other information entered by user is posted in api body including io address and on success otp dialog appears
    which auto reads the otp or self enter, and on submiting OTP api is called to verify the user and on success user is redirected
    to login screen.
 */
public class ReferFriend extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ReferFriend.class.getSimpleName();
    private ImageView imgBack, imgThankYou;
    private TextView txtToolbarTitle, txt_address_hint;
    private AppCompatTextView tvShowHideAddress;
    private Button btnSubmit;
    private TextInputEditText ietFriendsName, ietFriendsMobile;
    AppCompatEditText ietAddress;
    private RadioGroup rgAddress;
    private RadioButton rbAddressSame, rbAddressOther;
    private String mobile;
    private Dialog mDialog;
    private String setReferFriendEndUrl = "v1/refer_friend";
    private String upgradeEndUrl = "v1/upgrade";
    private String typeAddress = "";
    private static StringBuilder hexString;
    String userSerial, apiKey;
    AutocompleteSupportFragment autocompleteFragment;
    TextInputLayout ilAddressAutoFragment;
    LinearLayout ilAddress;
    String lat, lng, autocompleteFragmentAddress;
    private boolean isKeyboardVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_refer_friend);

        initPlacesApi();
        ilAddressAutoFragment = (TextInputLayout) findViewById(R.id.ilAddressAutoFragment);
        ilAddress = (LinearLayout) findViewById(R.id.ilAddress);
        txtToolbarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
        txt_address_hint = (TextView) findViewById(R.id.txt_address_hint);
        txtToolbarTitle.setText("Refer A Friend");
        ietFriendsName = findViewById(R.id.ietFriendsName);
        ietFriendsMobile = findViewById(R.id.ietFriendsMobile);
        rgAddress = findViewById(R.id.rgAddress);
        rbAddressSame = findViewById(R.id.rbAddressSame);
        rbAddressOther = findViewById(R.id.rbAddressOther);
        ietAddress = findViewById(R.id.ietAddress);
        btnSubmit = findViewById(R.id.btnSubmit);
        imgBack = findViewById(R.id.imgBack);
        imgThankYou = findViewById(R.id.imgThankYou);
        tvShowHideAddress = findViewById(R.id.tvShowHideAddress);

        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(ReferFriend.this, userSerialEncrypted);
        apiKey = dduSharedPref.getString("apiKey", "");

        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {
                isKeyboardVisible = isVisible;
            }
        });
        imgBack.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        tvShowHideAddress.setOnClickListener(this);
    }

    private void initPlacesApi() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        //String apiKeyOld = "AIzaSyDIIcIhkh5TtUSIB2h92rVfXcn4u14HCBo";
        String apiKey = "AIzaSyAyp2O8KiiD-wsnmiy9f1qsgHLuqfDZOSs";
        // Initialize the SDK
        Places.initialize(getApplicationContext(), apiKey);
        // Create a new Places client instance
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(fields);
            autocompleteFragment.setCountry("IN");
            final View search_view = autocompleteFragment.getView();
            if (search_view != null) {
                search_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((TextView) v.findViewById(R.id.places_autocomplete_search_input)).setText("");
                    }
                });
                ((EditText) search_view.findViewById(R.id.places_autocomplete_search_input)).setTextSize(12.0f);
                ((View) search_view.findViewById(R.id.places_autocomplete_search_button)).setVisibility(View.GONE);
                /*((View) search_view.findViewById(R.id.places_autocomplete_clear_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((EditText) search_view.findViewById(R.id.places_autocomplete_search_input)).setText("");
//                        ((TextView) v.findViewById(R.id.places_autocomplete_search_input)).setText("");
                        tvTechType.setText("");
                    }
                });*/
            }


            // Set up a PlaceSelectionListener to handle the response.
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(final Place place) {
                    // TODO: Get info about the selected place.
                    Log.e(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getLatLng());
//                    tvTechType.setText("");
                    if (search_view != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                String tmp = place.getName() + ", " + place.getAddress();
                                ((EditText) search_view.findViewById(R.id.places_autocomplete_search_input)).setText(tmp);
                                autocompleteFragmentAddress = tmp;
                            }
                        }, 300);
                    }
                    LatLng placeLatLng = place.getLatLng();

                    DecimalFormat df = new DecimalFormat("#.######");
                    if (placeLatLng != null) {
                        lat = df.format(placeLatLng.latitude);
                        lng = df.format(placeLatLng.longitude);
                    }
                }

                @Override
                public void onError(Status status) {
                    // TODO: Handle the error.
                    Log.i(TAG, "An error occurred: " + status);
                }
            });
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        ((RadioButton) view).setChecked(true);
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.rbAddressSame:
                if (checked) {
                    ietAddress.setText("");
                    autocompleteFragmentAddress = "";
                    typeAddress = "same";
                    ilAddressAutoFragment.setVisibility(View.GONE);
                    ilAddress.setVisibility(View.VISIBLE);
                    tvShowHideAddress.setVisibility(View.GONE);
                    txt_address_hint.setVisibility(View.VISIBLE);

                    if (NetworkUtils.isNetworkConnected(ReferFriend.this)) {
                        callUpgradeApi();
                    } else {
                        Toast.makeText(ReferFriend.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.rbAddressOther:
                if (checked) {
                    // Ninjas rule
                    ietAddress.setText("");
                    autocompleteFragmentAddress = "";
//                    lat="";
//                    lng="";
                    typeAddress = "other";
                    ilAddressAutoFragment.setVisibility(View.VISIBLE);
                    ilAddress.setVisibility(View.GONE);
                    tvShowHideAddress.setVisibility(View.GONE);
                    txt_address_hint.setVisibility(View.GONE);
                }
                break;
        }
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
                if (NetworkUtils.isNetworkConnected(ReferFriend.this)) {
                    if (isValidated()) {
                        callReferFriendApi();
                    }
                } else {
                    Toast.makeText(ReferFriend.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tvShowHideAddress:
                if (!rbAddressSame.isChecked() || !rbAddressOther.isChecked()) {
                    //alert
                    Toast.makeText(this, "Please select any option first.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private boolean isValidated() {
        boolean flag = false;
        if (ietFriendsName != null && TextUtils.isEmpty(ietFriendsName.getText().toString().trim())) {
            Toast.makeText(this, "Fields can not be empty!", Toast.LENGTH_SHORT).show();
        } else if (ietFriendsName != null && !ietFriendsName.getText().toString().matches("[a-zA-Z. ]*")) {
            Toast.makeText(this, "Name must not contain special characters", Toast.LENGTH_SHORT).show();
        } else if (ietFriendsMobile != null && TextUtils.isEmpty(ietFriendsMobile.getText().toString().trim())) {
            Toast.makeText(this, "Fields can not be empty!", Toast.LENGTH_SHORT).show();
        } else if (ietFriendsMobile != null && ietFriendsMobile.getText().length() < 10) {
            Toast.makeText(this, "Invalid Mobile Number!", Toast.LENGTH_SHORT).show();
        } else if (typeAddress != null && TextUtils.isEmpty(typeAddress)) {
            Toast.makeText(this, "Fields can not be empty!", Toast.LENGTH_SHORT).show();
        } else if ((ietAddress != null && TextUtils.isEmpty(ietAddress.getText().toString().trim())) && (TextUtils.isEmpty(autocompleteFragmentAddress.trim()))) {
            Toast.makeText(this, "Fields can not be empty!", Toast.LENGTH_SHORT).show();
        } else {
            flag = true;
        }
        return flag;
    }

    private void callUpgradeApi() {
        JSONObject mainJObject = new JSONObject();

        try {
            mainJObject.put("serial", userSerial);

        } catch (JSONException ignored) {
        }

        try {
            mDialog = UIUtils.showProgressDialog(ReferFriend.this);
            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + upgradeEndUrl;
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
                                        String username = jsonObject.optString("name");
                                        String upgrade = jsonObject.optString("upgrade");
                                        String address = jsonObject.optString("address");

                                        ietAddress.setText(address);
                                    }
                                } else {
                                    Toast.makeText(ReferFriend.this, msg, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(ReferFriend.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(ReferFriend.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(ReferFriend.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    private void callReferFriendApi() {

        JSONObject mainJObject = new JSONObject();

        try {
            mainJObject.put("serial", userSerial);
            mainJObject.put("name", ietFriendsName.getText());
            mainJObject.put("mobile", ietFriendsMobile.getText());
            mainJObject.put("type", typeAddress);
            if (typeAddress.equalsIgnoreCase("same")) {
                mainJObject.put("address", ietAddress.getText());
            } else if (typeAddress.equalsIgnoreCase("other")) {
                if (autocompleteFragmentAddress != null && !autocompleteFragmentAddress.equalsIgnoreCase("")) {
                    mainJObject.put("address", autocompleteFragmentAddress);
                    mainJObject.put("lat", lat);
                    mainJObject.put("lng", lng);
                }
            }
        } catch (JSONException ignored) {
        }
        final String mRequestBody = mainJObject.toString();

        try {
            mDialog = UIUtils.showProgressDialog(ReferFriend.this);
            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + setReferFriendEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, mainJObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            if (response != null) {
                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                if (statusCode.equals("200")) {
                                    ietAddress.setText("");
                                    ietFriendsMobile.setText("");
                                    ietFriendsName.setText("");
                                    tvShowHideAddress.setVisibility(View.VISIBLE);
                                    ilAddressAutoFragment.setVisibility(View.GONE);
                                    ilAddress.setVisibility(View.GONE);
                                    imgThankYou.setVisibility(View.VISIBLE);
                                    rgAddress.clearCheck();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            imgThankYou.setVisibility(View.GONE);
                                        }
                                    }, 5000);
//                                    Toast.makeText(ReferFriend.this, msg, Toast.LENGTH_SHORT).show();
                                    /*JSONObject jsonObject = response.optJSONObject("data");
                                    if (jsonObject != null) {

                                    }*/

                                } else {
                                    UIUtils.dismissDialog(mDialog);
                                    Toast.makeText(ReferFriend.this, msg, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(ReferFriend.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(ReferFriend.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(ReferFriend.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ReferFriend.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }
}
