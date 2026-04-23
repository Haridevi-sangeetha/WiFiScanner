
//package com.example.myapplication;
//
//import org.json.JSONObject;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.io.OutputStream;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import android.Manifest;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.net.wifi.ScanResult;
//import android.net.wifi.WifiManager;
//import android.os.Bundle;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//
//import java.util.List;
//
//public class MainActivity extends AppCompatActivity {
//
//    TextView textView;
//    WifiManager wifiManager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        textView = findViewById(R.id.textView);
//        textView.setText("Loading WiFi...");
//
//        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//
//        // Delay scan (VERY IMPORTANT)
//        textView.postDelayed(() -> checkPermissionAndScan(), 2000);
//    }
//
//    private void checkPermissionAndScan() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//        } else {
//            scanWifi();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if (requestCode == 1 && grantResults.length > 0
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//            scanWifi();
//        } else {
//            textView.setText("Permission Denied ❌");
//        }
//    }
//
//    private void scanWifi() {
//        try {
//            if (wifiManager == null) {
//                textView.setText("WiFi not supported ❌");
//                return;
//            }
//
//            List<ScanResult> results = wifiManager.getScanResults();
//
//            if (results == null || results.size() == 0) {
//                textView.setText("No WiFi Found ❌");
//                return;
//            }
//
//            StringBuilder builder = new StringBuilder();
//
//            for (ScanResult result : results) {
//                if (result.SSID != null && !result.SSID.isEmpty()) {
//                    builder.append("SSID: ").append(result.SSID).append("\n");
//                    builder.append("Signal: ").append(result.level).append("\n\n");
//
//                    sendToServer(result.SSID, result.level);
//                }
//            }
//
//            if (builder.length() == 0) {
//                textView.setText("Hidden networks only ❌");
//            } else {
//                textView.setText(builder.toString());
//            }
//
//        } catch (Exception e) {
//            textView.setText("Error: " + e.getMessage());
//        }
//    }
//
//    private void sendToServer(String ssid, int signal) {
//
//        new Thread(() -> {
//            try {
//                URL url = new URL("http://10.233.208.139:5000/analyze");
//
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Content-Type", "application/json");
//                conn.setDoOutput(true);
//
//                String json = "{"
//                        + "\"ssid\":\"" + ssid + "\","
//                        + "\"signal\":\"" + signal + "\","
//                        + "\"security\":\"WPA2\""
//                        + "}";
//
//                OutputStream os = conn.getOutputStream();
//                os.write(json.getBytes());
//                os.flush();
//
//                BufferedReader br = new BufferedReader(
//                        new InputStreamReader(conn.getInputStream())
//                );
//
//                // 🔥 FIX: read full response
//                StringBuilder sb = new StringBuilder();
//                String line;
//
//                while ((line = br.readLine()) != null) {
//                    sb.append(line);
//                }
//
//                String response = sb.toString();
//
//                runOnUiThread(() -> {
//                    try {
//                        JSONObject obj = new JSONObject(response);
//
//                        String ssidRes = obj.getString("ssid");
//                        String signalRes = obj.getString("signal");
//                        String risk = obj.getString("risk");
//
//                        textView.append(
//                                "\nSSID : " + ssidRes +
//                                        "\nSignal : " + signalRes +
//                                        "\nRisk : " + risk +
//                                        "\n"
//                        );
//
//                    } catch (Exception e) {
//                        textView.append("\nParse Error: " + e.getMessage());
//                    }
//                });
//
//            } catch (Exception e) {
//                runOnUiThread(() -> {
//                    textView.append("\nError: " + e.getMessage());
//                });
//            }
//        }).start();
//    }
//}


//package com.example.myapplication;
//
//import android.Manifest;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.graphics.Color;
//import android.net.wifi.ScanResult;
//import android.net.wifi.WifiManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.speech.tts.TextToSpeech;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.List;
//import java.util.Locale;
//
//public class MainActivity extends AppCompatActivity {
//
//    TextView textView;
//    WifiManager wifiManager;
//    Handler handler = new Handler();
//
//    TextToSpeech tts;
//
//    int scanInterval = 5000; // 🔥 5 seconds auto scan
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        textView = findViewById(R.id.textView);
//        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//
//        // 🔊 Voice alert setup
//        tts = new TextToSpeech(this, status -> {
//            if (status == TextToSpeech.SUCCESS) {
//                tts.setLanguage(Locale.US);
//            }
//        });
//
//        checkPermissionAndScan();
//    }
//
//    // 🔥 AUTO SCAN LOOP
//    Runnable scanRunnable = new Runnable() {
//        @Override
//        public void run() {
//            scanWifi();
//            handler.postDelayed(this, scanInterval);
//        }
//    };
//
//    private void checkPermissionAndScan() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//        } else {
//            handler.post(scanRunnable); // 🔥 start auto scan
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if (requestCode == 1 && grantResults.length > 0
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//            handler.post(scanRunnable);
//        }
//    }
//
//    private void scanWifi() {
//
//        try {
//            List<ScanResult> results = wifiManager.getScanResults();
//
//            StringBuilder graph = new StringBuilder();
//            graph.append("📶 WiFi Strength Graph:\n\n");
//
//            for (ScanResult result : results) {
//
//                if (result.SSID != null && !result.SSID.isEmpty()) {
//
//                    // 🔥 simple graph using bars
//                    int bars = Math.abs(result.level) / 10;
//                    graph.append(result.SSID).append(" : ");
//
//                    for (int i = 0; i < bars; i++) {
//                        graph.append("█");
//                    }
//
//                    graph.append(" (").append(result.level).append(" dBm)\n");
//
//                    sendToServer(result.SSID, result.level);
//                }
//            }
//
//            textView.setText(graph.toString());
//
//        } catch (Exception e) {
//            textView.setText("Error: " + e.getMessage());
//        }
//    }
//
//    private void sendToServer(String ssid, int signal) {
//
//        new Thread(() -> {
//            try {
//
//                URL url = new URL("http://10.233.208.139:5000/analyze");
//
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Content-Type", "application/json");
//                conn.setDoOutput(true);
//
//                String json = "{"
//                        + "\"ssid\":\"" + ssid + "\","
//                        + "\"signal\":\"" + signal + "\","
//                        + "\"security\":\"WPA2\""
//                        + "}";
//
//                OutputStream os = conn.getOutputStream();
//                os.write(json.getBytes());
//                os.flush();
//
//                BufferedReader br = new BufferedReader(
//                        new InputStreamReader(conn.getInputStream())
//                );
//
//                StringBuilder sb = new StringBuilder();
//                String line;
//
//                while ((line = br.readLine()) != null) {
//                    sb.append(line);
//                }
//
//                String response = sb.toString();
//
//                runOnUiThread(() -> {
//
//                    try {
//
//                        JSONObject obj = new JSONObject(response);
//
//                        String risk = obj.getString("risk");
//
//                        // 🔥 VOICE ALERT
//                        if (risk.equals("DANGEROUS")) {
//                            tts.speak("Dangerous WiFi detected", TextToSpeech.QUEUE_FLUSH, null, null);
//                        }
//
//                        // 🔥 COLOR CHANGE
//                        if (risk.equals("SAFE")) {
//                            textView.setTextColor(Color.GREEN);
//                        } else if (risk.equals("MEDIUM")) {
//                            textView.setTextColor(Color.YELLOW);
//                        } else {
//                            textView.setTextColor(Color.RED);
//                        }
//
//                    } catch (Exception e) {
//                        textView.append("\nParse Error: " + e.getMessage());
//                    }
//
//                });
//
//            } catch (Exception e) {
//                runOnUiThread(() ->
//                        textView.append("\nError: " + e.getMessage())
//                );
//            }
//        }).start();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        handler.removeCallbacks(scanRunnable);
//        if (tts != null) tts.shutdown();
//    }
//}
//
//

package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    Button scanBtn;

    TextView netCount, safeCount, riskCount;

    WifiManager wifiManager;
    Handler handler = new Handler();

    boolean scanning = false;

    ArrayList<String> wifiList;
    ArrayAdapter<String> adapter;

    int safe = 0, risk = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        scanBtn = findViewById(R.id.scanBtn);

        netCount = findViewById(R.id.netCount);
        safeCount = findViewById(R.id.safeCount);
        riskCount = findViewById(R.id.riskCount);

        wifiManager = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        wifiList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                wifiList);

        listView.setAdapter(adapter);

        checkPermission();

        scanBtn.setOnClickListener(v -> {

            scanning = !scanning;

            if (scanning) {
                scanBtn.setText("STOP SCAN");
                startLoop();
            } else {
                scanBtn.setText("START SCAN");
                handler.removeCallbacksAndMessages(null);
            }
        });
    }

    private void startLoop() {

        handler.post(new Runnable() {
            @Override
            public void run() {

                if (!scanning) return;

                scanWifi();

                handler.postDelayed(this, 3000);
            }
        });
    }

    private void scanWifi() {

        try {

            List<ScanResult> results = wifiManager.getScanResults();

            wifiList.clear();

            safe = 0;
            risk = 0;

            if (results == null || results.isEmpty()) {
                wifiList.add("No WiFi Found");
                adapter.notifyDataSetChanged();
                return;
            }

            for (ScanResult r : results) {

                if (r.SSID != null && !r.SSID.isEmpty()) {

                    String status = "SAFE";

                    if (r.level < -70) {
                        status = "DANGER";
                        risk++;
                    } else {
                        safe++;
                    }

                    wifiList.add(
                            "SSID: " + r.SSID +
                                    "\nSignal: " + r.level +
                                    "\nStatus: " + status
                    );
                }
            }

            netCount.setText(results.size() + "\nNetworks");
            safeCount.setText(safe + "\nSafe");
            riskCount.setText(risk + "\nRisk");

            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            wifiList.clear();
            wifiList.add("Error: " + e.getMessage());
            adapter.notifyDataSetChanged();
        }
    }

    private void checkPermission() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1
            );
        }
    }
}