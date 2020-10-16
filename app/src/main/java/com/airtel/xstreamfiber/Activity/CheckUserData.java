package com.airtel.xstreamfiber.Activity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.Constant;
import com.airtel.xstreamfiber.Util.MethodsUtil;
import com.airtel.xstreamfiber.Util.UIUtils;
import com.airtel.xstreamfiber.view.SpeedometerGauge;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class CheckUserData extends AppCompatActivity {

    private ImageView imgBack;
    private TextView txtToolbarTitle;
    private String userSerial;
    private String apiKey;
    private Dialog mDialog;
    private String getCheckUserData = "v1/check_userData_new";
    private TextView tvPack, tvPackValue;
    private TextView tvPackBalance, tvPackBalanceValue;
    private TextView tvBalancedData, tvTotalData;
    private TextView tvPackDetail1, tvPackDetail2, tvPackDetail3, tvPackDetail4;
    private TextView tvPackDetail1Value, tvPackDetail2Value, tvPackDetail3Value, tvPackDetail4Value;
    private static DecimalFormat df = new DecimalFormat("0.0");
    private static DecimalFormat df_0 = new DecimalFormat("0");
    private BarChart chart;
    private View viewPartition;
    private TextView tvUsageGraph, tvTitleYaxis, tvNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_user_data);

        txtToolbarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
        txtToolbarTitle.setText("Data Usage");
        imgBack = (ImageView) findViewById(R.id.imgBack);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tvPack = findViewById(R.id.tvPack);
        tvPackBalance = findViewById(R.id.tvPackBalance);
        tvPackValue = findViewById(R.id.tvPackValue);
        tvPackBalanceValue = findViewById(R.id.tvPackBalanceValue);
        tvBalancedData = findViewById(R.id.tvBalancedData);
        tvTotalData = findViewById(R.id.tvTotalData);
        tvPackDetail1 = findViewById(R.id.tvPackDetail1);
        tvPackDetail2 = findViewById(R.id.tvPackDetail2);
        tvPackDetail3 = findViewById(R.id.tvPackDetail3);
        tvPackDetail4 = findViewById(R.id.tvPackDetail4);
        tvPackDetail1Value = findViewById(R.id.tvPackDetail1Value);
        tvPackDetail2Value = findViewById(R.id.tvPackDetail2Value);
        tvPackDetail3Value = findViewById(R.id.tvPackDetail3Value);
        tvPackDetail4Value = findViewById(R.id.tvPackDetail4Value);
        chart = findViewById(R.id.chartUsage);
        viewPartition = findViewById(R.id.viewPartition);
        tvUsageGraph = findViewById(R.id.tvUsageGraph);
        tvTitleYaxis = findViewById(R.id.tvTitleYaxis);
        tvNote = findViewById(R.id.tvNote);

        initchart();

        SharedPreferences dduSharedPref = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
//        userSerial = dduSharedPref.getString("userSerial", "");
        String userSerialEncrypted = dduSharedPref.getString("userSerial", "");
        userSerial = MethodsUtil.decryptSerial(getApplicationContext(), userSerialEncrypted);

        apiKey = dduSharedPref.getString("apiKey", "");

        if (savedInstanceState != null) {
            String tvPackValue = savedInstanceState.getString("tvPackValue");
            String tvPackBalanceValue = savedInstanceState.getString("tvPackBalanceValue");
            String tvBalancedData = savedInstanceState.getString("tvBalancedData");
            String tvTotalData = savedInstanceState.getString("tvTotalData");
            if (tvPackValue != null) {
                this.tvPackValue.setText(tvPackValue);
            }
            if (tvPackBalanceValue != null) {
                this.tvPackBalanceValue.setText(tvPackBalanceValue);
            }
            if (tvBalancedData != null) {
                this.tvBalancedData.setText(tvBalancedData);
            }
            if (tvTotalData != null) {
                this.tvTotalData.setText(tvTotalData);
            }
        } else {
            checkUserData();
        }
    }

    private void initchart() {
        showVisibility(false, chart,tvUsageGraph, tvTitleYaxis,viewPartition,tvNote);
        chart.getDescription().setEnabled(false);
        chart.setMaxVisibleValueCount(60);
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
//        leftAxis.setDrawTopYLabelEntry(false);
//        leftAxis.setAxisMaximum(500f);
//        leftAxis.setLabelCount(5);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setDrawLabels(false);
        leftAxis.setTextColor(getResources().getColor(android.R.color.white));
        leftAxis.setAxisLineColor(getResources().getColor(android.R.color.white));
//        leftAxis.setDrawAxisLine(false);
//        leftAxis.setEnabled(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMaximum(500f);
        rightAxis.setLabelCount(5);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        rightAxis.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
//        xAxis.setLabelCount(3);
        xAxis.setTextColor(getResources().getColor(android.R.color.white));
        xAxis.setAxisLineColor(getResources().getColor(android.R.color.white));
        /*xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return months[(int) value % months.length];
            }
        });*/
        chart.getAxisLeft().setDrawGridLines(false);

        // add a nice and smooth animation
        chart.animateY(1500);

        chart.getLegend().setEnabled(false);

        /*for (IDataSet set : chart.getData().getDataSets())
            set.setDrawValues(!set.isDrawValuesEnabled());

        chart.invalidate();*/
    }

    void showVisibility(boolean b, View... views) {
        for (View v : views) {
            if (b)
                v.setVisibility(View.VISIBLE);
            else
                v.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (tvPackBalanceValue != null && !tvPackBalanceValue.getText().toString().equalsIgnoreCase("")) {
            outState.putString("tvPackBalanceValue", tvPackBalanceValue.getText().toString());
        }
        if (tvPackValue != null && !tvPackValue.getText().toString().equalsIgnoreCase("")) {
            outState.putString("tvPackValue", tvPackValue.getText().toString());
        }
        if (tvBalancedData != null && !tvBalancedData.getText().toString().equalsIgnoreCase("")) {
            outState.putString("tvBalancedData", tvBalancedData.getText().toString());
        }
        if (tvTotalData != null && !tvTotalData.getText().toString().equalsIgnoreCase("")) {
            outState.putString("tvTotalData", tvTotalData.getText().toString());
        }
    }

    private void setGaugeDetail(String dataBalance, String dataTotal) {
        double dataBalancePercent = (Double.parseDouble(dataBalance) / Double.parseDouble(dataTotal)) * 100;
        SpeedometerGauge speedometer = (SpeedometerGauge) findViewById(R.id.viewGauge);
        speedometer.setMaxSpeed(50);
        speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });
        speedometer.setMaxSpeed(100);
        speedometer.setMajorTickStep(20);
        speedometer.setMinorTicks(3);
        speedometer.addColoredRange(0, 20, Color.RED);
        speedometer.addColoredRange(20, 70, Color.YELLOW);
        speedometer.addColoredRange(70, 100, Color.GREEN);
        speedometer.setSpeed(dataBalancePercent, 1000, 300);
    }


    private void checkUserData() {

        JSONObject mainJObject = new JSONObject();

        try {

            mainJObject.put("serial", userSerial);
        } catch (JSONException ignored) {
        }
        final String mRequestBody = mainJObject.toString();

        try {
            mDialog = UIUtils.showProgressDialog(CheckUserData.this);
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(false);

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = BuildConfig.baseUrl + getCheckUserData;
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

                                    JSONObject usageSpecification = null;
                                    String balancedData = null, totalData = null, thottleSpeed = null, planSpeed = null;
                                    if (jsonObject != null) {
                                        usageSpecification = jsonObject.optJSONObject("usageSpecification");
                                        if (usageSpecification != null) {
                                            balancedData = usageSpecification.optString("balancedData");
                                            totalData = usageSpecification.optString("totalData");
                                            thottleSpeed = usageSpecification.optString("thottleSpeed");
                                            planSpeed = usageSpecification.optString("planSpeed");
                                        }
                                    }

                                    /*double balancedDataMB = 0;
                                    if (balancedData != null) {
                                        balancedDataMB = Double.parseDouble(balancedData) / 1024 / 1024 / 1024;
                                    }
                                    double totalDataMB = 0;
                                    if (totalData != null) {
                                        totalDataMB = Double.parseDouble(totalData) / 1024;
                                    }*/


//                                    String dataBalance = df.format(balancedDataMB);
//                                    String dataTotal = df.format(totalDataMB);
//                                    tvBalancedData.setText(dataBalance+ "GB");
//                                    tvTotalData.setText("of " + dataTotal + "GB is left");
                                    tvBalancedData.setText(balancedData);
                                    tvTotalData.setText("of " + totalData + " is left");

                                    int index = 0;
                                    String dataBalanceGauge = null;
                                    String dataTotalGauge = null;
                                    if (balancedData != null) {
                                        index = balancedData.toUpperCase().indexOf("GB");
                                        dataBalanceGauge = balancedData.substring(0, index).trim();
                                    }
                                    if (totalData != null) {
                                        index = totalData.toUpperCase().indexOf("GB");
                                        dataTotalGauge = totalData.substring(0, index).trim();
                                    }
                                    setGaugeDetail(dataBalanceGauge, dataTotalGauge);


//                                    setGaugeDetail(dataBalance, dataTotal);

                                    JSONObject customerBillingCycle = null;
                                    if (jsonObject != null) {
                                        customerBillingCycle = jsonObject.optJSONObject("customerBillingCycle");
                                    }
                                    String billCycle = null;
                                    if (customerBillingCycle != null) {
                                        billCycle = customerBillingCycle.optString("billCycle");
                                    }


                                    JSONObject serviceSpecification = null;
                                    if (jsonObject != null) {
                                        serviceSpecification = jsonObject.optJSONObject("serviceSpecification");
                                    }
                                    String validity = null;
                                    if (serviceSpecification != null) {
                                        validity = serviceSpecification.optString("validity");
                                    }
                                    tvPackBalanceValue.setText(balancedData);
                                    tvPackValue.setText(totalData);
                                    /*tvPackValue.setText(df_0.format(totalDataMB) + " GB");
                                    tvPackDetail1Value.setText(thottleSpeed);
                                    tvPackDetail2Value.setText(planSpeed);
                                    tvPackDetail3Value.setText(billCycle);
                                    tvPackDetail4Value.setText(validity);

                                    if (thottleSpeed != null) {
                                        if (thottleSpeed.isEmpty()) {
                                            tvPackDetail1.setVisibility(View.GONE);
                                            tvPackDetail1Value.setVisibility(View.GONE);
                                        } else {
                                            tvPackDetail1.setVisibility(View.VISIBLE);
                                            tvPackDetail1Value.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    if (planSpeed != null) {
                                        if (planSpeed.isEmpty()) {
                                            tvPackDetail2.setVisibility(View.GONE);
                                            tvPackDetail2Value.setVisibility(View.GONE);
                                        } else {
                                            tvPackDetail2.setVisibility(View.VISIBLE);
                                            tvPackDetail2Value.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    if (billCycle != null) {
                                        if (billCycle.isEmpty()) {
                                            tvPackDetail3.setVisibility(View.GONE);
                                            tvPackDetail3Value.setVisibility(View.GONE);
                                        } else {
                                            tvPackDetail3.setVisibility(View.VISIBLE);
                                            tvPackDetail3Value.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    if (validity != null) {
                                        if (validity.isEmpty()) {
                                            tvPackDetail4.setVisibility(View.GONE);
                                            tvPackDetail4Value.setVisibility(View.GONE);
                                        } else {
                                            tvPackDetail4.setVisibility(View.VISIBLE);
                                            tvPackDetail4Value.setVisibility(View.VISIBLE);
                                        }
                                    }*/

                                    //graph usage
                                    JSONArray jsonObjectUsage = response.optJSONArray("usage");
                                    if (jsonObjectUsage != null && jsonObjectUsage.length()>0) {
                                        ArrayList<String> usageDataArray = new ArrayList<>();
                                        for (int i = 0; i < jsonObjectUsage.length(); i++) {
                                            usageDataArray.add(jsonObjectUsage.optString(i).trim());
                                        }
                                        String billing_cycle = response.optString("billing_cycle");
                                        if (!TextUtils.isEmpty(billing_cycle)){
                                            showVisibility(true, tvNote);
                                            tvNote.setText(billing_cycle);
                                        }
                                        showVisibility(true, chart,tvUsageGraph, tvTitleYaxis,viewPartition);
                                        graphPartMethod(usageDataArray);
                                    }

                                } else {
                                    UIUtils.dismissDialog(mDialog);
                                    Toast.makeText(CheckUserData.this, msg, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(CheckUserData.this, msg, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CheckUserData.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
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
                    UIUtils.dismissDialog(mDialog);
                    throw error;
                }
            });
            queue.add(getRequest);

        } catch (Exception ignored) { // for caught any exception during the excecution of the service
        }
    }

    private void graphPartMethod(ArrayList<String> usageDataArray) {
        final ArrayList<String> listMonth = new ArrayList<String>();
        ArrayList<BarEntry> values = new ArrayList<>();
        for (String data :
                usageDataArray) {
            spanAndAddMethod(data, values, listMonth);
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return listMonth.get((int) value % listMonth.size());
            }
        });

        BarDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(values, "Data Usage 2020");

            set1.setColor(Color.rgb(60, 220, 78));
//            set1.setValueTextColor(Color.rgb(60, 220, 78));
            set1.setValueTextColor(Color.WHITE);
            set1.setValueTextSize(10f);
            set1.setAxisDependency(YAxis.AxisDependency.LEFT);

            set1.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return value +" GB";
                }
            });

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setBarWidth(.3f);

            chart.setData(data);
            chart.setFitBars(true);
        }


        if (chart.getData() != null)
            for (IDataSet set : chart.getData().getDataSets())
            {
                set.setDrawValues(true);
            }

        chart.invalidate();
    }

    private void spanAndAddMethod(String data, ArrayList<BarEntry> values, ArrayList<String> listMonth) {

        StringTokenizer tokens = new StringTokenizer(data, " ");
        String[] verPartsString = new String[tokens.countTokens()];
        int count = 0;
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            token = token.replace("(", "");
            token = token.replace(")", "");
            verPartsString[count] = token;
            count++;
        }
        listMonth.add(verPartsString[1]);
        values.add(new BarEntry(listMonth.size() - 1, Float.parseFloat(verPartsString[0])));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        CheckUserData.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }

}
