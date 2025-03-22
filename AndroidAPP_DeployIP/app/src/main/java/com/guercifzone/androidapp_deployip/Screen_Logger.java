package com.guercifzone.androidapp_deployip;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Screen_Logger extends AppCompatActivity {
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_screen_logger);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize WebView
        webView = findViewById(R.id.webview);

        // Enable JavaScript
        webView.getSettings().setJavaScriptEnabled(true);

        // Allow Mixed Content (if your server is HTTP instead of HTTPS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // Enable DOM storage
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);

        // Set WebViewClient to handle internal links in the WebView itself
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                // Log or handle error (e.g., show a Toast)
                Log.e("WebViewError", "Error: " + error.getDescription());
                Toast.makeText(Screen_Logger.this, "Error loading page: " + error.getDescription(), Toast.LENGTH_LONG).show();

            }

        });

        // Set WebChromeClient to handle JavaScript and page loading events (optional)
        webView.setWebChromeClient(new WebChromeClient());

        // Load the URL (make sure both devices are on the same network)
        String url = "file:///android_asset/index.html";
        webView.loadUrl(url);

        // Enable WebView debugging (can inspect WebView via Chrome)
        WebView.setWebContentsDebuggingEnabled(true);
    }
    @Override
    public void onBackPressed() {
        // Handle the back button to go back in the WebView history
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}