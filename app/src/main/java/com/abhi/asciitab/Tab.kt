package com.abhi.asciitab

data class Tab(
    val id: Long = System.currentTimeMillis(),
    var title: String = "New Tab",
    var url: String = "",
    val incognito: Boolean = false,
    var thumbnail: android.graphics.Bitmap? = null,
    val webView: android.webkit.WebView
)
