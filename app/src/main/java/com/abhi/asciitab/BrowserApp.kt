package com.abhi.asciitab

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.abhi.asciitab.data.Tab
import com.abhi.asciitab.viewmodel.BrowserViewModel

enum class Screen { Browser, Tabs, Settings }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserApp(vm: BrowserViewModel, tabs: List<Tab>, activeId: Long?) {
    val context = LocalContext.current
    var screen by remember { mutableStateOf(Screen.Browser) }
    var urlInput by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val activeTab = tabs.find { it.id == activeId }

    LaunchedEffect(Unit) {
        if (vm.tabs.value.isEmpty()) vm.createTab()
    }

    Scaffold(
        topBar = {
            if (screen == Screen.Browser) {
                TopAppBar(
                    title = {
                        TextField(
                            value = urlInput.ifBlank { activeTab?.url ?: "" },
                            onValueChange = { urlInput = it },
                            placeholder = { Text("Search or type URL") },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Go),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onGo = {
                                val q = urlInput.trim()
                                if (q.isNotEmpty()) {
                                    val url = if (q.matches(Regex("^[\\\\w-]+(\\\\.[\\\\w-]+)+(:\\\\d+)?(/\\\\S*)?$"))) {
                                        if (q.startsWith("http")) q else "https://$q"
                                    } else if (q.startsWith("http://") || q.startsWith("https://")) q
                                    else "https://www.google.com/search?q=" + java.net.URLEncoder.encode(q, "UTF-8")
                                    activeTab?.webView?.loadUrl(url)
                                    vm.updateUrl(activeTab!!.id, url)
                                    urlInput = ""
                                }
                            }),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    actions = {
                        IconButton(onClick = { screen = Screen.Tabs }) {
                            Icon(Icons.Default.GridView, contentDescription = "Tabs")
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (screen == Screen.Browser) {
                BottomAppBar {
                    IconButton(onClick = { activeTab?.webView?.goBack() }) { Icon(Icons.Default.ArrowBack, null) }
                    IconButton(onClick = { activeTab?.webView?.goForward() }) { Icon(Icons.Default.ArrowForward, null) }
                    IconButton(onClick = { activeTab?.webView?.reload() }) { Icon(Icons.Default.Refresh, null) }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { activeTab?.webView?.loadUrl("file:///android_asset/newtab.html") }) { Icon(Icons.Default.Home, null) }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (screen) {
                Screen.Browser -> {
                    val wv = activeTab?.webView
                    if (activeTab != null) {
                        key(activeTab.id) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        setBackgroundColor(android.graphics.Color.BLACK)
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.allowFileAccess = true
                                        val url = activeTab.url.ifBlank { "file:///android_asset/newtab.html" }
                                        loadUrl(url)
                                        vm.attachWebView(activeTab.id, this)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                Screen.Tabs -> TabGridScreen(vm, tabs, activeId) { screen = Screen.Browser }
                Screen.Settings -> SettingsScreen { screen = Screen.Browser }
            }
        }
    }

    if (showMenu) {
        ModalBottomSheet(onDismissRequest = { showMenu = false }) {
            Column(Modifier.padding(16.dp)) {
                Text("Menu", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                MenuItem("New tab") { vm.createTab(); showMenu = false }
                MenuItem("New private tab") { vm.createTab(true); showMenu = false }
                MenuItem("Bookmarks") { showMenu = false }
                MenuItem("History") { showMenu = false }
                MenuItem("Settings") { screen = Screen.Settings; showMenu = false }
            }
        }
    }
}

@Composable
fun MenuItem(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun TabGridScreen(vm: BrowserViewModel, tabs: List<Tab>, activeId: Long?, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tabs (${tabs.size})", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        tabs.forEach { tab ->
            Card(
                onClick = { vm.setActive(tab.id); onBack() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(tab.title.ifBlank { "New Tab" }, modifier = Modifier.weight(1f))
                    IconButton(onClick = { vm.closeTab(tab.id) }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = { vm.createTab() }, modifier = Modifier.fillMaxWidth()) {
            Text("+ New tab")
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Theme, search engine, etc. (coming)")
        Spacer(Modifier.height(8.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}
