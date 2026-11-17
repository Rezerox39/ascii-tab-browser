package com.abhi.asciitab.data

import android.graphics.Bitmap
import android.webkit.WebView

data class Tab(
    val id: Long = System.currentTimeMillis(),
    var title: String = "New Tab",
    var url: String = "",
    val incognito: Boolean = false,
    var thumbnail: Bitmap? = null,
    var webView: WebView? = null
)
