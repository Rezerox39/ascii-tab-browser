package com.abhi.asciitab

data class Tab(
    val id: Long = System.currentTimeMillis(),
    var title: String = "New Tab",
    var url: String = "",
    val incognito: Boolean = false,
    val webView: android.webkit.WebView
)
