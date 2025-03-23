package com.guercifzone.androidapp_deployip;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

 Button btnScreen, btnWebCam,btnFolder;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnScreen = findViewById(R.id.btn_screen);
        btnWebCam = findViewById(R.id.btn_webcam);
       btnFolder = findViewById(R.id.btn_folder);


        btnScreen.setOnClickListener(v -> {
           startActivity(new Intent(MainActivity.this, Screen_Logger.class));
        });
        btnWebCam.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, WebCamView.class));
        });
        btnFolder.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FolderShow.class));
        });

        }
    }

