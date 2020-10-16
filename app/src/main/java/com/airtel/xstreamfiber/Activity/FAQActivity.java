package com.airtel.xstreamfiber.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airtel.xstreamfiber.BuildConfig;
import com.airtel.xstreamfiber.R;

/*
    In FAQ, just showing the data from given url in a webview.
*/
public class FAQActivity extends AppCompatActivity {

    private TextView txtToolbarTitle;
    private WebView webView;
    private String my_url= BuildConfig.baseUrl + "v2/faq";
    private ImageView imgBack;
    private final int MAX_PROGRESS = 100;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        progressBar = findViewById(R.id.progressBar);
        txtToolbarTitle=(TextView)findViewById(R.id.txtToolbarTitle);
        imgBack=(ImageView)findViewById(R.id.imgBack);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        txtToolbarTitle.setText("FAQ");

        webView = (WebView) findViewById(R.id.webView);
        speedTest();
        setWebClient();
    }

    public void speedTest()
    {
        webView.setVisibility(View.VISIBLE);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                return !my_url.equals(Uri.parse(url).getHost());
            }

            @Override
            public void onPageFinished(WebView view, final String url) {

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                //super.onReceivedError(view, request, error);
                webView.loadUrl("about:blank");
            }
        });
        webView.clearCache(true); //added to remove cache
        webView.loadUrl(my_url);
    }


    private void setWebClient() {
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if (newProgress < MAX_PROGRESS && progressBar.getVisibility() == ProgressBar.GONE){
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }
                if (newProgress == MAX_PROGRESS) {
                    progressBar.setVisibility(ProgressBar.GONE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FAQActivity.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }
}
