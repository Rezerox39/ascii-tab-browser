package com.abhi.asciitab

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
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
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

class MainActivity : AppCompatActivity() {

    private lateinit var container: FrameLayout
    private lateinit var urlBar: EditText
    private lateinit var progress: ProgressBar
    private lateinit var tabCount: TextView

    private val tabs = mutableListOf<Tab>()
    private var activeTab: Tab? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.webContainer)
        urlBar = findViewById(R.id.urlBar)
        progress = findViewById(R.id.progress)
        tabCount = findViewById(R.id.tabCount)
        findViewById<ImageButton>(R.id.moreBtn).setOnClickListener { showMenu() }

        urlBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadUrlOrSearch(urlBar.text.toString())
                true
            } else false
        }

        findViewById<ImageButton>(R.id.backBtn).setOnClickListener { activeTab?.webView?.let { if (it.canGoBack()) it.goBack() } }
        findViewById<ImageButton>(R.id.fwdBtn).setOnClickListener { activeTab?.webView?.let { if (it.canGoForward()) it.goForward() } }
        findViewById<ImageButton>(R.id.reloadBtn).setOnClickListener { activeTab?.webView?.reload() }
        findViewById<ImageButton>(R.id.homeBtn).setOnClickListener { openNewTab() }
        tabCount.setOnClickListener { showTabSwitcher() }

        openNewTab()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(incognito: Boolean = false): WebView {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val jsEnabled = prefs.getBoolean("js", true)
        val night = prefs.getBoolean("night", true)
        val wv = WebView(this)
        wv.settings.apply {
            javaScriptEnabled = jsEnabled
            domStorageEnabled = true
            allowFileAccess = !incognito
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        if (night && WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(wv.settings, WebSettingsCompat.FORCE_DARK_ON)
        }

        wv.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (view == activeTab?.webView) {
                    progress.progress = newProgress
                    progress.visibility = if (newProgress < 100) android.view.View.VISIBLE else android.view.View.GONE
                }
            }
            override fun onReceivedTitle(view: WebView?, title: String?) {
                tabs.find { it.webView == view }?.title = title ?: "New Tab"
            }
        }

        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (view == activeTab?.webView) {
                    if (url?.startsWith("file:///android_asset") == true) urlBar.setText("")
                    else urlBar.setText(url ?: "")
                }
                tabs.find { it.webView == view }?.url = url ?: ""
                val incognito = tabs.find { it.webView == view }?.incognito == true
                if (!incognito && !url.isNullOrBlank() && !url.startsWith("file:///android_asset")) {
                    getSharedPreferences("history", Context.MODE_PRIVATE).edit().putString(url, url).apply()
                }
            }
            override fun shouldInterceptRequest(view: WebView?, request: android.webkit.WebResourceRequest?): WebResourceResponse? {
                val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
                if (!prefs.getBoolean("adblock", true)) return null
                val host = request?.url?.host ?: return null
                if (AdBlocker.isBlocked(host)) {
                    return WebResourceResponse("text/plain", "UTF-8", "".byteInputStream())
                }
                return null
            }
        }

        wv.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            val req = android.app.DownloadManager.Request(android.net.Uri.parse(url))
            req.setMimeType(mimetype)
            req.addRequestHeader("User-Agent", userAgent)
            req.setDescription("Downloading...")
            req.setTitle(android.net.Uri.parse(url).lastPathSegment ?: "download")
            req.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            req.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, android.net.Uri.parse(url).lastPathSegment ?: "download")
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
            dm.enqueue(req)
        }

        return wv
    }

    private fun openNewTab(incognito: Boolean = false) {
        val wv = createWebView(incognito)
        val tab = Tab(incognito = incognito, webView = wv)
        tabs.add(tab)
        switchToTab(tab)
        wv.loadUrl("file:///android_asset/newtab.html")
        updateTabCount()
    }

    private fun switchToTab(tab: Tab) {
        container.removeAllViews()
        container.addView(tab.webView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        activeTab = tab
        if (tab.url.startsWith("file:///android_asset") || tab.url.isEmpty()) urlBar.setText("")
        else urlBar.setText(tab.url)
        updateTabCount()
    }

    private fun showTabSwitcher() {
        val items = tabs.map { it.title.take(40) }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Tabs")
            .setItems(items) { _, which -> switchToTab(tabs[which]) }
            .setPositiveButton("New tab") { _, _ -> openNewTab() }
            .setNegativeButton("Close tab") { _, _ ->
                activeTab?.let { closeTab(it) }
            }
            .show()
    }

    private fun closeTab(tab: Tab) {
        tabs.remove(tab)
        if (tab.incognito) {
            android.webkit.CookieManager.getInstance().removeAllCookies(null)
            tab.webView.clearCache(true)
            tab.webView.clearHistory()
        }
        tab.webView.destroy()
        if (tabs.isEmpty()) openNewTab()
        else switchToTab(tabs.last())
        updateTabCount()
    }

    private fun updateTabCount() {
        tabCount.text = tabs.size.toString()
    }

    private fun showMenu() {
        val anchor = findViewById<ImageButton>(R.id.moreBtn)
        val popup = android.widget.PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "New tab")
        popup.menu.add(0, 8, 0, "New incognito tab")
        popup.menu.add(0, 2, 0, "Bookmark this page")
        popup.menu.add(0, 3, 0, "Bookmarks")
        popup.menu.add(0, 4, 0, "History")
        popup.menu.add(0, 5, 0, "Find in page")
        popup.menu.add(0, 6, 0, "Reader mode")
        popup.menu.add(0, 7, 0, "Settings")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> openNewTab()
                8 -> openNewTab(true)
                2 -> bookmarkCurrent()
                3 -> showBookmarks()
                4 -> showHistory()
                5 -> findInPage()
                6 -> toggleReader()
                7 -> openSettings()
            }
            true
        }
        popup.show()
    }

    private fun bookmarkCurrent() {
        val url = activeTab?.url ?: return
        val title = activeTab?.title ?: url
        val prefs = getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
        prefs.edit().putString(url, title).apply()
        android.widget.Toast.makeText(this, "Bookmarked", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun showBookmarks() {
        val prefs = getSharedPreferences("bookmarks", Context.MODE_PRIVATE)
        val entries = prefs.all.map { "${it.value} — ${it.key}" }
        AlertDialog.Builder(this)
            .setTitle("Bookmarks")
            .setItems(entries.toTypedArray()) { _, which ->
                val url = entries[which].substringAfter(" — ")
                activeTab?.webView?.loadUrl(url)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showHistory() {
        val prefs = getSharedPreferences("history", Context.MODE_PRIVATE)
        val entries = prefs.all.keys.toList()
        AlertDialog.Builder(this)
            .setTitle("History")
            .setItems(entries.toTypedArray()) { _, which ->
                activeTab?.webView?.loadUrl(entries[which])
            }
            .setNegativeButton("Clear", { _, _ -> prefs.edit().clear().apply() })
            .show()
    }

    private fun findInPage() {
        val input = android.widget.EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Find in page")
            .setView(input)
            .setPositiveButton("Find") { _, _ ->
                activeTab?.webView?.findAllAsync(input.text.toString())
            }
            .setNegativeButton("Clear") { _, _ -> activeTab?.webView?.clearMatches() }
            .show()
    }

    private fun toggleReader() {
        val wv = activeTab?.webView ?: return
        val css = "body{max-width:700px;margin:0 auto;padding:24px;font-size:18px;line-height:1.6;background:#111;color:#eee;} img{max-width:100%;} nav,aside,footer,header,.ad{display:none!important;}"
        val js = "javascript:(function(){var s=document.createElement('style');s.innerHTML=\"$css\";document.head.appendChild(s);})()"
        wv.evaluateJavascript(js, null)
    }

    private fun openSettings() {
        startActivity(android.content.Intent(this, SettingsActivity::class.java))
    }

    private fun loadUrlOrSearch(input: String) {
        val wv = activeTab?.webView ?: return
        val q = input.trim()
        if (q.isEmpty()) return
        val url = if (q.matches(Regex("^[\\w-]+(\\.[\\w-]+)+(:\\d+)?(/\\S*)?$"))) {
            if (q.startsWith("http")) q else "https://$q"
        } else if (q.startsWith("http://") || q.startsWith("https://")) {
            q
        } else {
            "https://www.google.com/search?q=" + java.net.URLEncoder.encode(q, "UTF-8")
        }
        wv.loadUrl(url)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            activeTab?.webView?.let { if (it.canGoBack()) { it.goBack(); return true } }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        tabs.forEach { it.webView.destroy() }
        super.onDestroy()
    }
}
