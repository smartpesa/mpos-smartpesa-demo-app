package com.smartpesa.smartpesa.activity;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.base.BaseActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

public class WebViewActivity extends BaseActivity {

    WebView webView;
    ImageView backIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        initializeComponents();
        String urlToLoad = getIntent().getStringExtra("urlToLoad");
        webView.loadUrl(urlToLoad);
    }

    private void initializeComponents(){
        webView =(WebView) findViewById(R.id.webview);
        backIV = (ImageView) findViewById(R.id.webViewBackIV);
        backIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

}
