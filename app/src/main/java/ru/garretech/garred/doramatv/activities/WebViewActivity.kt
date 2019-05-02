package ru.garretech.garred.doramatv.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import ru.garretech.garred.doramatv.R
import android.webkit.WebView
import android.webkit.WebResourceRequest
import android.os.Build
import android.annotation.TargetApi
import android.util.Log
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_web_view.*




class WebViewActivity : AppCompatActivity() {
    val mWebViewClient by lazy { object : WebViewClient() {
        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            view.loadUrl(request.url.toString())
            return true
        }

        // Для старых устройств
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }


    }}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val url = intent.getStringExtra("link")
        setContentView(R.layout.activity_web_view)
        webView.webViewClient = mWebViewClient
        webView.settings.javaScriptEnabled = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.settings.allowFileAccessFromFileURLs = true
        Log.d("webView","Loading url:${url}")
        webView.loadUrl(url)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            webView.destroy()
            super.onBackPressed()
        }
    }
}
