package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    In HeatMapActivity, firstly wifi connection is checked if connected to WiFi then data such as link rate, wifi strength, SSID
    and score are fetched through local android function and displayed and if not connected to wifi then same is shown in the value place.
    On clicking or tapping on each blocks user can get the strength in specific rooms and save the report by clicking save report,
    if no changes are made then save report button is disabled. User can also change the room names as per there wish.
*/
public class HeatMapActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    SharedPreferences mPrefs;
    SharedPreferences.Editor mPrefsEditor;
    TextView tvRoom1, tvRoom2, tvRoom3, tvRoom4, tvRoom5, tvRoom6, tvRoom7, tvRoom8, tvScore1, tvScore2, tvScore3, tvScore4, tvScore5, tvScore6;
    private final String ROOM1 = "ROOM1";
    private final String ROOM2 = "ROOM2";
    private final String ROOM3 = "ROOM3";
    private final String ROOM4 = "ROOM4";
    private final String ROOM5 = "ROOM5";
    private final String ROOM6 = "ROOM6";
    private final String ROOM7 = "ROOM7";
    private final String ROOM8 = "ROOM8";
    private String heatMapAEndUrl = "v1/heat_map";
    private Dialog mDialog;
    private TextView txtToolbarTitle, note, tvIfPoor;
    private ImageView imgBack;
    private TextView tvStrength1, tvStrength2, tvStrength3, tvStrength4, tvStrength5, tvStrength6, tvStrength7, tvStrength8;
    private TextView tvVal_rssi, tvVal_ssid, tvVal_link_rate, tvSignalStrength;
    private int rssi, score, level;
    private String strength = "";
    TextView tvStrength, buttonDisableTv;
    private String channelName;
    private String title;
    private Button btn_save_report;
    private String userSerial;
    private String apiKey;
    private int height, width, configuration;
    private RelativeLayout rl_banner;

    //Getting the Wifi information after each 5 seconds
    Handler handler = new Handler();
    Runnable timedTask = new Runnable() {
        @Override
        public void run() {
            getWifiManagerData();
            handler.postDelayed(timedTask, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);

        tvStrength = findViewById(R.id.tvStrength);

        rl_banner = findViewById(R.id.rl_banner);

        tvVal_rssi = findViewById(R.id.tvVal_rssi);
        tvVal_ssid = findViewById(R.id.tvVal_ssid);
        tvVal_link_rate = findViewById(R.id.tvVal_link_rate);
        tvSignalStrength = findViewById(R.id.tvSignalStrength);

        buttonDisableTv = findViewById(R.id.button_disable_tv);

        note = findViewById(R.id.note);
        tvIfPoor = findViewById(R.id.tvIfPoor);


        txtToolbarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
        txtToolbarTitle.setText("WiFi Coverage Analyzer");

        SharedPreferences localcache = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = localcache.edit();
        channelName = localcache.getString("channelName", "");

//        imgBack = (ImageView) findViewById(R.id.backButton);
        imgBack = (ImageView) findViewById(R.id.imgBack);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        LinearLayout llLivingRoom = findViewById(R.id.llLivingRoom);
        LinearLayout llBedRoom1 = findViewById(R.id.llBedRoom1);
        LinearLayout llBedRoom2 = findViewById(R.id.llBedRoom2);
        LinearLayout llBedRoom3 = findViewById(R.id.llBedRoom3);
        LinearLayout llDinningRoom = findViewById(R.id.llDinningRoom);
        LinearLayout llBathRoom = findViewById(R.id.llBathRoom);
        LinearLayout llKitchen = findViewById(R.id.llKitchen);
        LinearLayout llOtherRoom = findViewById(R.id.llOtherRoom);

        tvRoom1 = findViewById(R.id.tvRoom1);
        tvRoom2 = findViewById(R.id.tvRoom2);
        tvRoom3 = findViewById(R.id.tvRoom3);
        tvRoom4 = findViewById(R.id.tvRoom4);
        tvRoom5 = findViewById(R.id.tvRoom5);
        tvRoom6 = findViewById(R.id.tvRoom6);
        tvRoom7 = findViewById(R.id.tvRoom7);
        tvRoom8 = findViewById(R.id.tvRoom8);

        tvStrength1 = findViewById(R.id.tvStrength1);
        tvStrength2 = findViewById(R.id.tvStrength2);
        tvStrength3 = findViewById(R.id.tvStrength3);
        tvStrength4 = findViewById(R.id.tvStrength4);
        tvStrength5 = findViewById(R.id.tvStrength5);
        tvStrength6 = findViewById(R.id.tvStrength6);
        tvStrength7 = findViewById(R.id.tvStrength7);
        tvStrength8 = findViewById(R.id.tvStrength8);

        btn_save_report = findViewById(R.id.btn_save_report);
        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);
        apiKey = dduSharedPref.getString("apiKey", "");

        mPrefs = getSharedPreferences(userSerial, MODE_PRIVATE);
        mPrefsEditor = mPrefs.edit();

        //If the user had already checked the strength and went back to main screen then there data will be saved when they again return to heatMap
        String strength1, strength2, strength3, strength4, strength5, strength6, strength7, strength8;
        strength1 = mPrefs.getString("Strength1", "Tap to test");
        strength2 = mPrefs.getString("Strength2", "Tap to test");
        strength3 = mPrefs.getString("Strength3", "Tap to test");
        strength4 = mPrefs.getString("Strength4", "Tap to test");
        strength5 = mPrefs.getString("Strength5", "Tap to test");
        strength6 = mPrefs.getString("Strength6", "Tap to test");
        strength7 = mPrefs.getString("Strength7", "Tap to test");
        strength8 = mPrefs.getString("Strength8", "Tap to test");

        tvStrength1.setText(strength1);
        tvStrength2.setText(strength2);
        tvStrength3.setText(strength3);
        tvStrength4.setText(strength4);
        tvStrength5.setText(strength5);
        tvStrength6.setText(strength6);
        tvStrength7.setText(strength7);
        tvStrength8.setText(strength8);

        if (!(strength1.equals("Tap to test") && strength2.equals("Tap to test") && strength3.equals("Tap to test") && strength4.equals("Tap to test") && strength5.equals("Tap to test") && strength6.equals("Tap to test") && strength7.equals("Tap to test") && strength8.equals("Tap to test"))) {
            btn_save_report.setEnabled(true);
            btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round));
            buttonDisableTv.setVisibility(View.GONE);
        }

        /*if ((strength1.equals("Weak") || strength2.equals("Weak") || strength3.equals("Weak") || strength4.equals("Weak") || strength5.equals("Weak") || strength6.equals("Weak") || strength7.equals("Weak") || strength8.equals("Weak"))) {
            tvIfPoor.setVisibility(View.VISIBLE);
        } else {
            tvIfPoor.setVisibility(View.GONE);
        }*/


        llLivingRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRSSI();
                tvStrength1.setText(strength);
//                if (strength.equals("Weak"))
//                    tvIfPoor.setVisibility(View.VISIBLE);
                mPrefsEditor.putString("Strength1", strength).apply();
                mPrefsEditor.putString("rssi1", "" + rssi).apply();

                String strength1 = mPrefs.getString("Strength1", "Tap to test");
                if (level == 0)   //If wifi not connected then disable button
                {
                    btn_save_report.setEnabled(false);
                    btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
                    buttonDisableTv.setVisibility(View.VISIBLE);
                } else {
                    if (!(strength1.isEmpty() || strength1.equalsIgnoreCase("Tap to test") || strength1.equalsIgnoreCase("WiFi not connected"))) {
                        btn_save_report.setEnabled(true);
                        btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round));
                        buttonDisableTv.setVisibility(View.GONE);
                    }
                }
            }
        });
        llBedRoom1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRSSI();
                tvStrength2.setText(strength);
//                if (strength.equals("Weak"))
//                    tvIfPoor.setVisibility(View.VISIBLE);
                mPrefsEditor.putString("Strength2", strength).apply();
                mPrefsEditor.putString("rssi2", "" + rssi).apply();

                String strength2 = mPrefs.getString("Strength2", "Tap to test");
                if (level == 0) {
                    btn_save_report.setEnabled(false);
                    btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
                    buttonDisableTv.setVisibility(View.VISIBLE);
                } else {
                    if (!(strength2.isEmpty() || strength2.equalsIgnoreCase("Tap to test") || strength2.equalsIgnoreCase("WiFi not connected"))) {
                        btn_save_report.setEnabled(true);
                        btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round));
                        buttonDisableTv.setVisibility(View.GONE);
                    }
                }
            }
        });
        llBedRoom2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRSSI();
                tvStrength7.setText(strength);
//                if (strength.equals("Weak"))
//                    tvIfPoor.setVisibility(View.VISIBLE);
                mPrefsEditor.putString("Strength7", strength).apply();
                mPrefsEditor.putString("rssi7", "" + rssi).apply();

                String strength7 = mPrefs.getString("Strength7", "Tap to test");

                if (level == 0) {
                    btn_save_report.setEnabled(false);
                    btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
                    buttonDisableTv.setVisibility(View.VISIBLE);
                } else {
                    if (!(strength7.isEmpty() || strength7.equalsIgnoreCase("Tap to test") || strength7.equalsIgnoreCase("WiFi not connected"))) {
                        btn_save_report.setEnabled(true);
                        btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round));
                        buttonDisableTv.setVisibility(View.GONE);
                    }
                }
            }
        });
        llBedRoom3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRSSI();
                tvStrength8.setText(strength);
//                if (strength.equals("Weak"))
//                    tvIfPoor.setVisibility(View.VISIBLE);
                mPrefsEditor.putString("Strength8", strength).apply();
                mPrefsEditor.putString("rssi8", "" + rssi).apply();

                String strength8 = mPrefs.getString("Strength8", "Tap to test");
                if (level == 0) {
                    btn_save_report.setEnabled(false);
                    btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
                    buttonDisableTv.setVisibility(View.VISIBLE);
                } else {
                    if (!(strength8.isEmpty() || strength8.equalsIgnoreCase("Tap to test") || strength8.equalsIgnoreCase("WiFi not connected"))) {
                        btn_save_report.setEnabled(true);
                        btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round));
                        buttonDisableTv.setVisibility(View.GONE);
                    }
                }
            }
        });
        llDinningRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRSSI();
                tvStrength3.setText(strength);
//                if (strength.equals("Weak"))
//                    tvIfPoor.setVisibility(View.VISIBLE);
                mPrefsEditor.putString("Strength3", strength).apply();
                mPrefsEditor.putString("rssi3", "" + rssi).apply();

                String strength3 = mPrefs.getString("Strength3", "Tap to test");

                if (level == 0) {
                    btn_save_report.setEnabled(false);
                    btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
                    buttonDisableTv.setVisibility(View.VISIBLE);
                } else {
                    if (!(strength3.isEmpty() || strength3.equalsIgnoreCase("Tap to test") || strength3.equalsIgnoreCase("WiFi not connected"))) {
                        btn_save_report.setEnabled(true);
                        btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round));
                        buttonDisableTv.setVisibility(View.GONE);
                    }
                }
            }
        });
        llBathRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRSSI();
                tvStrength4.setText(strength);
//                if (strength.equals("Weak"))
//                    tvIfPoor.setVisibility(View.VISIBLE);
                mPrefsEditor.putString("Strength4", strength).apply();
                mPrefsEditor.putString("rssi4", "" + rssi).apply();

                String strength4 = mPrefs.getString("Strength4", "Tap to test");

                if (level == 0) {
                    btn_save_report.setEnabled(false);
                    btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
                    buttonDisableTv.setVisibility(View.VISIBLE);
                } else {
                    if (!(strength4.isEmpty() || strength4.equalsIgnoreCase("Tap to test") || strength4.equalsIgnoreCase("WiFi not connected"))) {
                        btn_save_report.setEnabled(true);
                        btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round));
                        buttonDisableTv.setVisibility(View.GONE);
                    }
                }
            }
        });
        llKitchen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRSSI();
                tvStrength5.setText(strength);
//                if (strength.equals("Weak"))
//                    tvIfPoor.setVisibility(View.VISIBLE);
                mPrefsEditor.putString("Strength5", strength).apply();
                mPrefsEditor.putString("rssi5", "" + rssi).apply();

                String strength5 = mPrefs.getString("Strength5", "Tap to test");

                if (level == 0) {
                    btn_save_report.setEnabled(false);
                    btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
                    buttonDisableTv.setVisibility(View.VISIBLE);
                } else {
                    if (!(strength5.isEmpty() || strength5.equalsIgnoreCase("Tap to test") || strength5.equalsIgnoreCase("WiFi not connected"))) {
                        btn_save_report.setEnabled(true);
                        btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round));
                        buttonDisableTv.setVisibility(View.GONE);
                    }
                }
            }
        });
        llOtherRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRSSI();
                tvStrength6.setText(strength);
//                if (strength.equals("Weak"))
//                    tvIfPoor.setVisibility(View.VISIBLE);
                mPrefsEditor.putString("Strength6", strength).apply();
                mPrefsEditor.putString("rssi6", "" + rssi).apply();

                String strength6 = mPrefs.getString("Strength6", "Tap to test");
                if (level == 0) {
                    btn_save_report.setEnabled(false);
                    btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round_grey_disable));
                    buttonDisableTv.setVisibility(View.VISIBLE);
                } else {
                    if (!(strength6.isEmpty() || strength6.equalsIgnoreCase("Tap to test") || strength6.equalsIgnoreCase("WiFi not connected"))) {
                        btn_save_report.setEnabled(true);
                        btn_save_report.setBackground(getResources().getDrawable(R.drawable.button_round));
                        buttonDisableTv.setVisibility(View.GONE);
                    }

                }
            }
        });

        btn_save_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Checking if internet connected or not
                if (NetworkUtils.isNetworkConnected(HeatMapActivity.this)) {
                    networkCall();
                } else {
                    Toast.makeText(HeatMapActivity.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    public void getWifiManagerData() {
        ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI")) {
                if (netInfo.isConnected()) {

                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                    int rssi = wifiManager.getConnectionInfo().getRssi();
                    int linkRate = wifiManager.getConnectionInfo().getLinkSpeed();
                    String ssidWithQuotes = wifiManager.getConnectionInfo().getSSID();

                    String ssid = ssidWithQuotes.substring(1, ssidWithQuotes.length() - 1);


                    tvVal_link_rate.setText(linkRate + "Mbps");
                    tvVal_ssid.setText(ssid);

                    if (rssi >= -50) {
                        //Excellent signal
                        tvSignalStrength.setText("100");
                        tvVal_rssi.setText("Excellent");
                    } else if (rssi < -50 && rssi >= -55) {
                        //Excellent signal
                        tvSignalStrength.setText("90");
                        tvVal_rssi.setText("Excellent");
                    } else if (rssi < -55 && rssi >= -62) {
                        //Good signal
                        tvSignalStrength.setText("80");
                        tvVal_rssi.setText("Good");
                    } else if (rssi < -62 && rssi >= -65) {
                        //Good signal
                        tvSignalStrength.setText("75");
                        tvVal_rssi.setText("Good");
                    } else if (rssi < -65 && rssi >= -68) {
                        //Fair signal
                        tvSignalStrength.setText("70");
                        tvVal_rssi.setText("Fair");
                    } else if (rssi < -68 && rssi >= -74) {
                        //Fair signal
                        tvSignalStrength.setText("60");
                        tvVal_rssi.setText("Fair");
                    } else if (rssi < -74 && rssi >= -79) {
                        //Fair signal
                        tvSignalStrength.setText("60");
                        tvVal_rssi.setText("Fair");
                    } else if (rssi < -79 && rssi >= -83) {
                        //Weak signal
                        tvSignalStrength.setText("30");
                        tvVal_rssi.setText("Weak");
//                        tvIfPoor.setVisibility(View.VISIBLE);
                    } else if (rssi < -83) {
                        //Weak signal
                        tvSignalStrength.setText("10");
                        tvVal_rssi.setText("Weak");
//                        tvIfPoor.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvVal_rssi.setText("WiFi not connected");
                    tvVal_link_rate.setText("WiFi not connected");
                    tvVal_ssid.setText("WiFi not connected");
                    tvSignalStrength.setText("0");
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        tvRoom1.setText(mPrefs.getString(ROOM1, tvRoom1.getText().toString()));
        tvRoom2.setText(mPrefs.getString(ROOM2, tvRoom2.getText().toString()));
        tvRoom3.setText(mPrefs.getString(ROOM3, tvRoom3.getText().toString()));
        tvRoom4.setText(mPrefs.getString(ROOM4, tvRoom4.getText().toString()));
        tvRoom5.setText(mPrefs.getString(ROOM5, tvRoom5.getText().toString()));
        tvRoom6.setText(mPrefs.getString(ROOM6, tvRoom6.getText().toString()));
        tvRoom7.setText(mPrefs.getString(ROOM7, tvRoom7.getText().toString()));
        tvRoom8.setText(mPrefs.getString(ROOM8, tvRoom8.getText().toString()));

        ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI")) {
                if (netInfo.isConnected()) {
                    getWifiManagerData();
                } else {
                    Toast.makeText(HeatMapActivity.this, "Please connect to Home WiFi to proceed.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        handler.post(timedTask);
    }

    //Dialog for renaming the rooms name
    public void showCustomDialog(final String title) {
        final String[] tempTitle = new String[1];
        final Dialog dialog = new Dialog(HeatMapActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);

        TextView titleTxt = (TextView) dialog.findViewById(R.id.txt_title_dialog);
        titleTxt.setText("Rename");
        //   titleTxt.setVisibility(View.GONE);

        TextView msg = (TextView) dialog.findViewById(R.id.txt_msg_dialog);
        // msg.setTextSize(getResources().getDimension(R.dimen.sp16));
        msg.setVisibility(View.GONE);
        msg.setTypeface(null, Typeface.BOLD);
        msg.setText("You can rename");

        TextInputEditText inputText = dialog.findViewById(R.id.inputText);
        int maxLength = 17;
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        inputText.setFilters(fArray);
        inputText.setMaxLines(2);
        inputText.setText(title);

        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                tempTitle[0] = "avn";
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                tempTitle[0] = s.toString();

            }
        });

        Button positiveDialogButton = (Button) dialog.findViewById(R.id.btn_yes);
        positiveDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(inputText.getText().toString().trim())) {
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    if (title.equalsIgnoreCase(tvRoom1.getText().toString())) {
                        prefsEditor.putString(ROOM1, tempTitle[0]);
                        prefsEditor.apply();
                        tvRoom1.setText(mPrefs.getString(ROOM1, title));
                        dialog.dismiss();
                    } else if (title.equalsIgnoreCase(tvRoom2.getText().toString())) {
                        prefsEditor.putString(ROOM2, tempTitle[0]);
                        prefsEditor.apply();
                        tvRoom2.setText(mPrefs.getString(ROOM2, title));
                        dialog.dismiss();
                    } else if (title.equalsIgnoreCase(tvRoom3.getText().toString())) {
                        prefsEditor.putString(ROOM3, tempTitle[0]);
                        prefsEditor.apply();
                        tvRoom3.setText(mPrefs.getString(ROOM3, title));
                        dialog.dismiss();
                    } else if (title.equalsIgnoreCase(tvRoom4.getText().toString())) {
                        prefsEditor.putString(ROOM4, tempTitle[0]);
                        prefsEditor.apply();
                        tvRoom4.setText(mPrefs.getString(ROOM4, title));
                        dialog.dismiss();
                    } else if (title.equalsIgnoreCase(tvRoom5.getText().toString())) {
                        prefsEditor.putString(ROOM5, tempTitle[0]);
                        prefsEditor.apply();
                        tvRoom5.setText(mPrefs.getString(ROOM5, title));
                        dialog.dismiss();
                    } else if (title.equalsIgnoreCase(tvRoom6.getText().toString())) {
                        prefsEditor.putString(ROOM6, tempTitle[0]);
                        prefsEditor.apply();
                        tvRoom6.setText(mPrefs.getString(ROOM6, title));
                        dialog.dismiss();
                    } else if (title.equalsIgnoreCase(tvRoom7.getText().toString())) {
                        prefsEditor.putString(ROOM7, tempTitle[0]);
                        prefsEditor.apply();
                        tvRoom7.setText(mPrefs.getString(ROOM7, title));
                        dialog.dismiss();
                    } else if (title.equalsIgnoreCase(tvRoom8.getText().toString())) {
                        prefsEditor.putString(ROOM8, tempTitle[0]);
                        prefsEditor.apply();
                        tvRoom8.setText(mPrefs.getString(ROOM8, title));
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(HeatMapActivity.this, "Field can not be empty!!", Toast.LENGTH_SHORT).show();
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

    private void networkCall() {

        // getRSSI();

        String strength1, strength2, strength3, strength4, strength5, strength6, strength7, strength8;
        strength1 = mPrefs.getString("Strength1", "");
        strength2 = mPrefs.getString("Strength2", "");
        strength3 = mPrefs.getString("Strength3", "");
        strength4 = mPrefs.getString("Strength4", "");
        strength5 = mPrefs.getString("Strength5", "");
        strength6 = mPrefs.getString("Strength6", "");
        strength7 = mPrefs.getString("Strength7", "");
        strength8 = mPrefs.getString("Strength8", "");

        String rssi1, rssi2, rssi3, rssi4, rssi5, rssi6, rssi7, rssi8;
        rssi1 = mPrefs.getString("rssi1", "");
        rssi2 = mPrefs.getString("rssi2", "");
        rssi3 = mPrefs.getString("rssi3", "");
        rssi4 = mPrefs.getString("rssi4", "");
        rssi5 = mPrefs.getString("rssi5", "");
        rssi6 = mPrefs.getString("rssi6", "");
        rssi7 = mPrefs.getString("rssi7", "");
        rssi8 = mPrefs.getString("rssi8", "");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serial", userSerial);
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("area_name", tvRoom1.getText().toString());
            jsonObject1.put("rssi", rssi1);

            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("area_name", tvRoom2.getText().toString());
            jsonObject2.put("rssi", rssi2);


            JSONObject jsonObject3 = new JSONObject();
            jsonObject3.put("area_name", tvRoom3.getText().toString());
            jsonObject3.put("rssi", rssi3);


            JSONObject jsonObject4 = new JSONObject();
            jsonObject4.put("area_name", tvRoom4.getText().toString());
            jsonObject4.put("rssi", rssi4);


            JSONObject jsonObject5 = new JSONObject();
            jsonObject5.put("area_name", tvRoom5.getText().toString());
            jsonObject5.put("rssi", rssi5);


            JSONObject jsonObject6 = new JSONObject();
            jsonObject6.put("area_name", tvRoom6.getText().toString());
            jsonObject6.put("rssi", rssi6);

            JSONObject jsonObject7 = new JSONObject();
            jsonObject7.put("area_name", tvRoom7.getText().toString());
            jsonObject7.put("rssi", rssi7);

            JSONObject jsonObject8 = new JSONObject();
            jsonObject8.put("area_name", tvRoom8.getText().toString());
            jsonObject8.put("rssi", rssi8);

            JSONArray jsonArray = new JSONArray();

            if (!(strength1.isEmpty() || strength1.equalsIgnoreCase("Tap to test") || strength1.equalsIgnoreCase("WiFi not connected")))
                jsonArray.put(jsonObject1);
            if (!(strength2.isEmpty() || strength2.equalsIgnoreCase("Tap to test") || strength2.equalsIgnoreCase("WiFi not connected")))
                jsonArray.put(jsonObject2);
            if (!(strength3.isEmpty() || strength3.equalsIgnoreCase("Tap to test") || strength3.equalsIgnoreCase("WiFi not connected")))
                jsonArray.put(jsonObject3);
            if (!(strength4.isEmpty() || strength4.equalsIgnoreCase("Tap to test") || strength4.equalsIgnoreCase("WiFi not connected")))
                jsonArray.put(jsonObject4);
            if (!(strength5.isEmpty() || strength5.equalsIgnoreCase("Tap to test") || strength5.equalsIgnoreCase("WiFi not connected")))
                jsonArray.put(jsonObject5);
            if (!(strength6.isEmpty() || strength6.equalsIgnoreCase("Tap to test") || strength6.equalsIgnoreCase("WiFi not connected")))
                jsonArray.put(jsonObject6);
            if (!(strength7.isEmpty() || strength7.equalsIgnoreCase("Tap to test") || strength7.equalsIgnoreCase("WiFi not connected")))
                jsonArray.put(jsonObject7);
            if (!(strength8.isEmpty() || strength8.equalsIgnoreCase("Tap to test") || strength8.equalsIgnoreCase("WiFi not connected")))
                jsonArray.put(jsonObject8);

            jsonObject.put("heat_map", jsonArray);
        } catch (JSONException ignored) {
        }

        try {
            mDialog = UIUtils.showProgressDialog(HeatMapActivity.this);
            mDialog.setCancelable(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + heatMapAEndUrl;
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            UIUtils.dismissDialog(mDialog);
                            if (response != null) {

                                String statusCode = response.optString("StatusCode");
                                String msg = response.optString("Message");
                                if (statusCode.equals("200")) {
                                    Toast.makeText(HeatMapActivity.this, msg, Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(HeatMapActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(HeatMapActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(HeatMapActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(HeatMapActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getRSSI() {
        ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI")) {
                if (netInfo.isConnected()) {


                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    rssi = 0;
                    List<ScanResult> wifiList = null;
                    if (wifiManager != null) {
                        wifiList = wifiManager.getScanResults();
                        for (ScanResult scanResult : wifiList) {
                            int level = WifiManager.calculateSignalLevel(scanResult.level, 5);
                        }

                        rssi = wifiManager.getConnectionInfo().getRssi();
                        //  wifiManager.getConnectionInfo().getS

                        level = WifiManager.calculateSignalLevel(rssi, 5);
                        if (level >= 4) {
                            score = 98;
                            strength = "Excellent";
                        } else if (level >= 3) {
                            score = 90;
                            strength = "Good";
                        } else if (level >= 2) {
                            score = 85;
                            strength = "Fair";
                        } else if (level >= 1) {
                            score = 45;
                            strength = "Weak";
//                            tvIfPoor.setVisibility(View.VISIBLE);
                        } else if (level == 0) {
                            strength = "WiFi not connected";
                        }

                    }
                } else {
                    strength = "WiFi not connected";
                    Toast.makeText(HeatMapActivity.this, "Please connect to Home WiFi to proceed.", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HeatMapActivity.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }

    public void showPopup1(View view) {
        Context wrapper = new ContextThemeWrapper(HeatMapActivity.this, R.style.PopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.heatmap_menu, popup.getMenu());
        popup.show();
        title = tvRoom1.getText().toString();
    }

    public void showPopup2(View view) {
        Context wrapper = new ContextThemeWrapper(HeatMapActivity.this, R.style.PopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.heatmap_menu, popup.getMenu());
        popup.show();
        title = tvRoom2.getText().toString();
    }

    public void showPopup3(View view) {
        Context wrapper = new ContextThemeWrapper(HeatMapActivity.this, R.style.PopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.heatmap_menu, popup.getMenu());
        popup.show();
        title = tvRoom3.getText().toString();
    }

    public void showPopup4(View view) {
        Context wrapper = new ContextThemeWrapper(HeatMapActivity.this, R.style.PopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.heatmap_menu, popup.getMenu());
        popup.show();
        title = tvRoom4.getText().toString();
    }

    public void showPopup5(View view) {
        Context wrapper = new ContextThemeWrapper(HeatMapActivity.this, R.style.PopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.heatmap_menu, popup.getMenu());
        popup.show();

        title = tvRoom5.getText().toString();
    }

    public void showPopup6(View view) {
        Context wrapper = new ContextThemeWrapper(HeatMapActivity.this, R.style.PopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.heatmap_menu, popup.getMenu());
        popup.show();
        title = tvRoom6.getText().toString();

    }

    public void showPopup7(View view) {
        Context wrapper = new ContextThemeWrapper(HeatMapActivity.this, R.style.PopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.heatmap_menu, popup.getMenu());
        popup.show();
        title = tvRoom7.getText().toString();
    }

    public void showPopup8(View view) {
        Context wrapper = new ContextThemeWrapper(HeatMapActivity.this, R.style.PopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view);
        popup.setOnMenuItemClickListener(this::onMenuItemClick);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.heatmap_menu, popup.getMenu());
        popup.show();
        title = tvRoom8.getText().toString();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId() == R.id.action_rename) {
            //alertDialogCustom(title);
            showCustomDialog(title);
            return true;
        } else {
            return false;
        }
    }


}
