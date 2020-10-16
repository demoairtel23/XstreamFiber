package com.airtel.xstreamfiber.Activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airtel.xstreamfiber.R;

public class HowToRegister extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgBack;
    private TextView txtToolbarTitle;
    WebView webView;
    ProgressBar progressBar;
    private final int MAX_PROGRESS = 100;
    private final String PAGE_URL = "pageUrl";
    private String pageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_register);

        pageUrl = getIntent().getStringExtra(PAGE_URL);

        txtToolbarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
        txtToolbarTitle.setText("How To Register");
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        imgBack = (ImageView) findViewById(R.id.imgBack);
        imgBack.setOnClickListener(this);
        initWebView();
        setWebClient();
        handlePullToRefresh();
        webView.clearCache(true); //added to remove cache
        loadUrl(pageUrl);
    }

    private void loadUrl(String pageUrl) {
        webView.loadUrl(pageUrl);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()){
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    private void handlePullToRefresh() {

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

    private void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                //super.onReceivedError(view, request, error);
                webView.loadUrl("about:blank");
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imgBack) {
            onBackPressed();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HowToRegister.this.overridePendingTransition(R.anim.fade, R.anim.fadeout);
    }
}
