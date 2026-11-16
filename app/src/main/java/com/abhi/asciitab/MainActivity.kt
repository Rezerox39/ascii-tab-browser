package com.abhi.asciitab

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var progress: ProgressBar

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        urlBar = findViewById(R.id.urlBar)
        progress = findViewById(R.id.progress)
        webView = findViewById(R.id.webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        // Dark mode hint for sites that support prefers-color-scheme
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progress.progress = newProgress
                progress.visibility = if (newProgress < 100) android.view.View.VISIBLE else android.view.View.GONE
            }
            override fun onReceivedTitle(view: WebView?, title: String?) {
                if (!title.isNullOrBlank() && view?.url?.startsWith("file:///android_asset") != true) {
                    urlBar.setText(view?.url ?: "")
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false
            override fun onPageFinished(view: WebView?, url: String?) {
                if (url?.startsWith("file:///android_asset") == true) urlBar.setText("")
                else urlBar.setText(url ?: "")
            }
            // Lightweight request-level ad/tracker blocking (Via-inspired)
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val host = request?.url?.host ?: return null
                if (AdBlocker.isBlocked(host)) {
                    return WebResourceResponse("text/plain", "UTF-8", "".byteInputStream())
                }
                return null
            }
        }

        urlBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadUrlOrSearch(urlBar.text.toString())
                true
            } else false
        }

        findViewById<ImageButton>(R.id.backBtn).setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        findViewById<ImageButton>(R.id.fwdBtn).setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        findViewById<ImageButton>(R.id.reloadBtn).setOnClickListener { webView.reload() }
        findViewById<ImageButton>(R.id.homeBtn).setOnClickListener { openNewTab() }

        openNewTab()
    }

    private fun openNewTab() {
        webView.loadUrl("file:///android_asset/newtab.html")
        urlBar.setText("")
    }

    private fun loadUrlOrSearch(input: String) {
        val q = input.trim()
        if (q.isEmpty()) return
        val url = if (q.matches(Regex("^[\\w-]+(\\.[\\w-]+)+(:\\d+)?(/\\S*)?$"))) {
            if (q.startsWith("http")) q else "https://$q"
        } else if (q.startsWith("http://") || q.startsWith("https://")) {
            q
        } else {
            "https://www.google.com/search?q=" + java.net.URLEncoder.encode(q, "UTF-8")
        }
        webView.loadUrl(url)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
