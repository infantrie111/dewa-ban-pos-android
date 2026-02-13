# 📱 ANDROID WRAPPER - DEWA BAN POS

Project ini adalah "pembungkus" (wrapper) aplikasi Android Native untuk menjalankan PWA DEWA BAN dengan fitur akses penuh ke Hardware Bluetooth Printer.

## 🛠️ Persiapan

1.  **Download & Install Android Studio**: [https://developer.android.com/studio](https://developer.android.com/studio)
2.  **Buka Android Studio**:
    *   Pilih **Open**.
    *   Arahkan ke folder `android_wrapper` ini.
    *   Tunggu proses Sync Gradle selesai (membutuhkan internet).

## ⚙️ Konfigurasi URL

Sebelum di-build, Anda **HARUS** mengubah URL PWA di dalam file `MainActivity.java`.

1.  Buka `app > java > com.dewaban.pos > MainActivity`.
2.  Cari baris:
    ```java
    private static final String PWA_URL = "https://dewa-ban.web.app";
    ```
3.  Ganti dengan URL PWA Anda yang sebenarnya. Jika sedang development (localhost), pastikan gunakan IP Address Laptop (contoh: `http://192.168.1.5:5500`), **JANGAN** `localhost` karena `localhost` di Emulator/HP mengacu ke HP itu sendiri.

## 🚀 Cara Build APK (Debug/Release)

1.  Di menu atas Android Studio, klik **Build > Build Bundle(s) / APKs > Build APK(s)**.
2.  Tunggu proses selesai.
3.  Akan muncul notifikasi "APK(s) generated successfully". Klik **locate** untuk membuka foldernya.
4.  Copy file `app-debug.apk` ke HP Android Anda.
5.  Install dan jalankan.

## 🔗 Integrasi di PWA (Javascript)

Agar PWA bisa "berbicara" dengan aplikasi ini, update kode `app.js` Anda:

```javascript
/* =========================================
   Fungsi Cetak Hybrid (Support APK + Web)
   ========================================= */
function cetakStruk(transaksi) {
    // 1. Generate Data Struk (Array of Bytes) seperti di v53
    // ... kode generate bytes ...
    
    // 2. Convert Array Bytes ke Base64
    let binaryString = '';
    for (let i = 0; i < comandos.length; i++) {
        binaryString += String.fromCharCode(comandos[i]);
    }
    const base64Data = btoa(binaryString);

    // 3. Deteksi Lingkungan
    if (window.Android && window.Android.cetakStruk) {
        // A. JIKA DI DALAM APLIKASI ANDROID (APK)
        console.log("Mencetak via Native Bluetooth...");
        window.Android.cetakStruk(base64Data);
    } else {
        // B. JIKA DI BROWSER BIASA (Chrome PC/HP)
        // Fallback ke RawBT localhost atau tampilkan error
        console.log("Mencetak via RawBT / Browser...");
        kirimKeRawBT(base64Data); 
    }
}
```
