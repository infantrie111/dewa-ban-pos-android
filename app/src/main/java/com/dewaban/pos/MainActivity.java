package com.dewaban.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;

public class MainActivity extends AppCompatActivity {

    private WebView myWebView;
    // GANTI URL INI DENGAN URL PWA FIREBASE ANDA !!
    // Jika masih development, pastikan HP dan Laptop satu jaringan WiFi dan pakai IP Laptop (misal: http://192.168.1.5:5500)
    private static final String PWA_URL = "https://infantrie111.github.io/dewa-ban-kasir/"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup WebView Full Screen
        myWebView = new WebView(this);
        setContentView(myWebView);

        // Konfigurasi WebView
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Inject Interface "Android" ke dalam Javascript
        myWebView.addJavascriptInterface(new WebAppInterface(this, this), "Android");

        // Agar link tetap buka di aplikasi, bukan melempar ke Chrome
        myWebView.setWebViewClient(new WebViewClient());

        // Load URL
        myWebView.loadUrl(PWA_URL);

        // Request Izin Bluetooth saat pertama kali dibuka
        checkBluetoothPermissions();
    }

    private void checkBluetoothPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED || 
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                }, 1);
            }
        }
    }

    // Fungsi yang dipanggil dari WebAppInterface
    public void printFromJavascript(String base64Data) {
        runOnUiThread(() -> {
            try {
                // 0. Cek Izin Terlebih Dahulu (PENTING UNTUK ANDROID 12+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Izin Bluetooth belum diberikan! Harap terima izin di layar.", Toast.LENGTH_LONG).show();
                        checkBluetoothPermissions(); // Minta izin lagi
                        return;
                    }
                }

                // 1. Decode Base64 dari JS menjadi bytes
                byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);

                // 2. Cari Printer Bluetooth yang sedang terhubung/paired
                BluetoothConnection selectedConnection = null;
                try {
                    selectedConnection = BluetoothPrintersConnections.selectFirstPaired();
                } catch (SecurityException e) {
                    Toast.makeText(this, "Gagal akses Bluetooth: Izin ditolak.", Toast.LENGTH_LONG).show();
                    return;
                } catch (Exception e) {
                   Toast.makeText(this, "Gagal mencari printer: " + e.getMessage(), Toast.LENGTH_LONG).show();
                   return;
                }

                if (selectedConnection == null) {
                    Toast.makeText(this, "Printer Bluetooth tidak ditemukan! Pastikan Printer Nyala & sudah Paired di HP.", Toast.LENGTH_LONG).show();
                    return;
                }

                // 3. (Optional) Inisialisasi visual untuk log (tidak mandatory jika kita connect manual)
                // EscPosPrinter printer = new EscPosPrinter(selectedConnection, 203, 58, 32); 
                // KITA KOMEN KARENA KITA PAKAI RAW CONNECTION LANGSUNG

                // 4. Kirim Data RAW BYTES langsung ke Socket
                BluetoothConnection finalConnection = selectedConnection;
                new Thread(() -> {
                    try {
                        // Connect Low Level
                        // Perlu try-catch SecurityException lagi di dalam thread
                        try {
                            finalConnection.connect();
                            finalConnection.write(decodedBytes); 
                            finalConnection.disconnect();
                        } catch (SecurityException se) {
                             runOnUiThread(() -> Toast.makeText(this, "Security Error: Izin Bluetooth hilang.", Toast.LENGTH_LONG).show()); 
                             return;
                        }

                        runOnUiThread(() -> Toast.makeText(this, "Struk Terkirim 🖨️", Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Gagal Print: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                }).start();

            } catch (Exception e) {
                Toast.makeText(this, "Error General: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
