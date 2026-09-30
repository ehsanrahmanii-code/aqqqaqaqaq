package com.arsam.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_NOTIF = 1001;
    private static final String DASHBOARD = "http://127.0.0.1:8080/";

    private WebView webView;
    private View loadingPanel;
    private TextView statusText;
    private Button btnStorage;
    private Button btnRetry;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService probePool = Executors.newSingleThreadExecutor();
    private final AtomicBoolean dashboardShown = new AtomicBoolean(false);
    private int probeTries = 0;

    private final BroadcastReceiver engineReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (EngineService.ACTION_FAILED.equals(intent.getAction())) {
                String err = intent.getStringExtra(EngineService.EXTRA_ERROR);
                statusText.setText("خطای موتور: " + (err != null ? err : "unknown"));
                btnRetry.setVisibility(View.VISIBLE);
                btnStorage.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        loadingPanel = findViewById(R.id.loading_panel);
        statusText = findViewById(R.id.status_text);
        btnStorage = findViewById(R.id.btn_storage);
        btnRetry = findViewById(R.id.btn_retry);

        setupWebView();
        btnStorage.setOnClickListener(v -> openAllFilesSettings());
        btnRetry.setOnClickListener(v -> startEngineFlow());

        IntentFilter filter = new IntentFilter();
        filter.addAction(EngineService.ACTION_FAILED);
        ContextCompat.registerReceiver(this, engineReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        ensureAaaFolder();
        requestRuntimePermissions();
        startEngineFlow();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.setWebViewClient(new WebViewClient());
    }

    private void requestRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                statusText.setText(getString(R.string.need_storage));
                btnStorage.setVisibility(View.VISIBLE);
            }
        }
    }

    private void openAllFilesSettings() {
        try {
            Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            i.setData(Uri.parse("package:" + getPackageName()));
            startActivity(i);
        } catch (Exception e) {
            Intent i = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(i);
        }
    }

    private void ensureAaaFolder() {
        try {
            File aaa = new File(Environment.getExternalStorageDirectory(), "AAA");
            File[] subs = {
                    new File(aaa, "data"),
                    new File(aaa, "cache"),
                    new File(aaa, "memory"),
                    new File(aaa, "secrets")
            };
            //noinspection ResultOfMethodCallIgnored
            aaa.mkdirs();
            for (File f : subs) {
                //noinspection ResultOfMethodCallIgnored
                f.mkdirs();
            }
        } catch (Exception ignored) {
        }
    }

    private void startEngineFlow() {
        dashboardShown.set(false);
        probeTries = 0;
        btnRetry.setVisibility(View.GONE);
        loadingPanel.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        statusText.setText(R.string.loading);

        Intent svc = new Intent(this, EngineService.class);
        ContextCompat.startForegroundService(this, svc);
        scheduleProbe();
    }

    private void scheduleProbe() {
        handler.postDelayed(this::probeDashboard, 1500);
    }

    private void probeDashboard() {
        if (dashboardShown.get()) return;
        probeTries++;
        statusText.setText("راه‌اندازی موتور… (" + probeTries + ")");
        probePool.execute(() -> {
            boolean ok = httpOk(DASHBOARD);
            handler.post(() -> {
                if (ok) {
                    showDashboard();
                } else if (probeTries < 40) {
                    scheduleProbe();
                } else {
                    statusText.setText("موتور پاسخ نداد. مجوز حافظه AAA و اینترنت را بررسی کنید.");
                    btnRetry.setVisibility(View.VISIBLE);
                    btnStorage.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "ARSAM timeout", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private boolean httpOk(String url) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(1200);
            c.setReadTimeout(1200);
            c.setRequestMethod("GET");
            int code = c.getResponseCode();
            return code >= 200 && code < 500;
        } catch (Exception e) {
            return false;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private void showDashboard() {
        if (!dashboardShown.compareAndSet(false, true)) return;
        loadingPanel.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(DASHBOARD);
    }

    @Override
    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(engineReceiver);
        } catch (Exception ignored) {
        }
        probePool.shutdownNow();
        super.onDestroy();
    }
}
