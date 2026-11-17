package com.abhi.asciitab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.abhi.asciitab.data.Tab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BrowserViewModel(app: Application) : AndroidViewModel(app) {
    private val _tabs = MutableStateFlow<List<Tab>>(emptyList())
    val tabs: StateFlow<List<Tab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<Long?>(null)
    val activeTabId: StateFlow<Long?> = _activeTabId.asStateFlow()

    val activeTab: Tab? get() = _tabs.value.find { it.id == _activeTabId.value }

    fun createTab(incognito: Boolean = false): Tab {
        val tab = Tab(incognito = incognito)
        _tabs.value = _tabs.value + tab
        if (_activeTabId.value == null) _activeTabId.value = tab.id
        return tab
    }

    fun attachWebView(tabId: Long, webView: android.webkit.WebView) {
        val updated = _tabs.value.map { if (it.id == tabId) it.copy(webView = webView) else it }
        _tabs.value = updated
    }

    fun setActive(tabId: Long) {
        _activeTabId.value = tabId
    }

    fun closeTab(tabId: Long) {
        val tab = _tabs.value.find { it.id == tabId }
        tab?.webView?.stopLoading()
        tab?.webView?.destroy()
        val remaining = _tabs.value.filterNot { it.id == tabId }
        _tabs.value = remaining
        if (_activeTabId.value == tabId) {
            _activeTabId.value = remaining.lastOrNull()?.id
        }
    }

    fun updateTitle(tabId: Long, title: String) {
        _tabs.value = _tabs.value.map { if (it.id == tabId) it.copy(title = title) else it }
    }

    fun updateUrl(tabId: Long, url: String) {
        _tabs.value = _tabs.value.map { if (it.id == tabId) it.copy(url = url) else it }
    }

    fun captureThumbnail(tabId: Long, bitmap: android.graphics.Bitmap?) {
        _tabs.value = _tabs.value.map { if (it.id == tabId) it.copy(thumbnail = bitmap) else it }
    }
}
