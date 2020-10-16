package com.airtel.xstreamfiber.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;
import com.airtel.xstreamfiber.Util.NetworkUtils;

/*
    In FAQ, just showing the data from given url in a webview.
*/
public class HelpAndFeedbackActivity extends AppCompatActivity {

    private TextView txtToolbarTitle;
    private LinearLayout llContactUs, llFaq, llShare, llTrackRequest;
    private WebView webView;
    private String my_url= BuildConfig.baseUrl + "v2/faq";
    private ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        txtToolbarTitle=(TextView)findViewById(R.id.txtToolbarTitle);
        imgBack=(ImageView)findViewById(R.id.imgBack);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        txtToolbarTitle.setText("Help & Feedback");


        llContactUs=findViewById(R.id.llContactUs);
        llFaq=findViewById(R.id.llFaq);
        llShare=findViewById(R.id.llShare);
        llTrackRequest=findViewById(R.id.llTrackRequest);
        llContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isNetworkConnected(HelpAndFeedbackActivity.this)) {
                    Intent intent = new Intent(HelpAndFeedbackActivity.this, Feedback.class);
                    startActivity(intent);
                    HelpAndFeedbackActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
                } else {
                    Toast.makeText(HelpAndFeedbackActivity.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });
        llFaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isNetworkConnected(HelpAndFeedbackActivity.this)) {
                    Intent intent = new Intent(HelpAndFeedbackActivity.this, FAQActivity.class);
                    startActivity(intent);
                    HelpAndFeedbackActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
                } else {
                    Toast.makeText(HelpAndFeedbackActivity.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });
        llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String app_name = getResources().getString(R.string.app_name);
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "I'm using "+app_name+" APP. This APP can enhance Wi-Fi performance of Airtel Xstream Fiber. Check your Fiber network health, troubleshoot and raise service request from the comfort of your home with this App. Click  u.airtel.in/wfh";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, app_name+" APP");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });
        llTrackRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isNetworkConnected(HelpAndFeedbackActivity.this)) {
                    Intent intent = new Intent(HelpAndFeedbackActivity.this, TrackRequestActivity.class);
                    startActivity(intent);
                    HelpAndFeedbackActivity.this.overridePendingTransition(R.anim.zoom, R.anim.zoomout);
                } else {
                    Toast.makeText(HelpAndFeedbackActivity.this, getString(R.string.not_connected_to_internet), Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*SharedPreferences sharedPrefLogin = getSharedPreferences(Constant.LOGIN_PREF, MODE_PRIVATE);
        String m_button = sharedPrefLogin.getString("m_button", "");
        if (m_button.equalsIgnoreCase("show")){
            //show
            llCpeConfiguration.setVisibility(View.VISIBLE);
        }else if (m_button.equalsIgnoreCase("hide")){
            //hide
            llCpeConfiguration.setVisibility(View.GONE);
        }*/

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HelpAndFeedbackActivity.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }
}
