package com.dewaban.pos;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {
    Context mContext;
    MainActivity mActivity;

    /** Instantiate the interface and set the context */
    WebAppInterface(Context c, MainActivity activity) {
        mContext = c;
        mActivity = activity;
    }

    /** Jembatan 1: Tes Koneksi dari JS */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    /** Jembatan 2: Fungsi Utama Cetak Struk */
    @JavascriptInterface
    public void cetakStruk(String base64Data) {
        // Panggil fungsi di MainActivity untuk handle hardware
        mActivity.printFromJavascript(base64Data);
    }
}
