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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            }, 1);
        }
    }

    // Fungsi yang dipanggil dari WebAppInterface
    public void printFromJavascript(String base64Data) {
        runOnUiThread(() -> {
            try {
                // 1. Decode Base64 dari JS menjadi bytes
                byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);

                // 2. Cari Printer Bluetooth yang sedang terhubung/paired
                BluetoothConnection selectedConnection = BluetoothPrintersConnections.selectFirstPaired();

                if (selectedConnection == null) {
                    Toast.makeText(this, "Printer Bluetooth tidak ditemukan! Pastikan sudah Paired.", Toast.LENGTH_LONG).show();
                    return;
                }

                // 3. Inisialisasi Printer (Lebar 58mm, 32 Karakter per baris)
                EscPosPrinter printer = new EscPosPrinter(selectedConnection, 203, 58, 32);

                // 4. Kirim Data (Kita mengirim raw bytes yang sudah disusun di JS)
                // Library DantSu biasanya menerima formatted text, tapi kita bisa bypass dengan mengirim teks yang sudah di-encode
                // Trik: Karena kita sudah punya byte commands lengkap (ESC @, dll), 
                // kita bisa kirim langsung menggunakan write() dari connection socket jika perlu,
                // tapi library DantSu membungkusnya. 
                // Cara paling aman pakai library ini adalah membiarkan dia handle formatting, 
                // TAPI karena Anda sudah hardcode bytes di JS, kita gunakan method printFormattedTextAndCut() dengan Hex wrapper atau kirim raw.
                
                // UNTUK KASUS ANDA (Sudah punya Byte Array lengkap dari JS):
                // Kita gunakan method write() langsung ke socket connection agar commands ESC/POS Anda tidak diubah-ubah library.
                
                new Thread(() -> {
                    try {
                        printer.getConnection().connect();
                        printer.getConnection().write(decodedBytes);
                        printer.getConnection().disconnect();
                        runOnUiThread(() -> Toast.makeText(this, "Struk Terkirim 🖨️", Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Gagal Print: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                }).start();

            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
