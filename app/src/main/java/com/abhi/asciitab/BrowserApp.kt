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
    var showFind by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var bottomBarVisible by remember { mutableStateOf(true) }
    val activeTab = tabs.find { it.id == activeId }
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner, activeTab) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> activeTab?.webView?.onPause()
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> activeTab?.webView?.onResume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                        BadgedBox(badge = { Badge { Text(tabs.size.toString()) } }) {
                            IconButton(onClick = { screen = Screen.Tabs }) {
                                Icon(Icons.Default.GridView, contentDescription = "Tabs")
                            }
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
                androidx.compose.animation.AnimatedVisibility(visible = bottomBarVisible) {
                    BottomAppBar {
                        IconButton(onClick = { activeTab?.webView?.goBack() }) { Icon(Icons.Default.ArrowBack, null) }
                        IconButton(onClick = { activeTab?.webView?.goForward() }) { Icon(Icons.Default.ArrowForward, null) }
                        IconButton(onClick = { activeTab?.webView?.reload() }) { Icon(Icons.Default.Refresh, null) }
                        IconButton(onClick = {
                            val i = android.content.Intent(android.content.Intent.ACTION_SEND)
                            i.type = "text/plain"
                            i.putExtra(android.content.Intent.EXTRA_TEXT, activeTab?.url ?: "")
                            context.startActivity(android.content.Intent.createChooser(i, "Share"))
                        }) { Icon(Icons.Default.Share, null) }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { activeTab?.webView?.loadUrl("file:///android_asset/newtab.html") }) { Icon(Icons.Default.Home, null) }
                    }
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
                                        webViewClient = object : android.webkit.WebViewClient() {
                                            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                                if (url != null) vm.updateUrl(activeTab.id, url)
                                                view?.title?.let { vm.updateTitle(activeTab.id, it) }
                                            }
                                            override fun onReceivedError(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?, error: android.webkit.WebResourceError?) {
                                                if (request?.isForMainFrame == true) {
                                                    view?.loadData(ERROR_HTML, "text/html", "UTF-8")
                                                }
                                            }
                                        }
                                        setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                                            bottomBarVisible = scrollY <= oldScrollY || scrollY < 100
                                        }
                                        val url = activeTab.url.ifBlank { "file:///android_asset/newtab.html" }
                                        loadUrl(url)
                                        vm.attachWebView(activeTab.id, this)
                                    }
                                },
                                modifier = Modifier.fillMaxSize().padding(bottom = 72.dp)
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
                MenuItem("Find in page") { showFind = true; showMenu = false }
                MenuItem("Share") {
                    val i = android.content.Intent(android.content.Intent.ACTION_SEND)
                    i.type = "text/plain"
                    i.putExtra(android.content.Intent.EXTRA_TEXT, activeTab?.url ?: "")
                    context.startActivity(android.content.Intent.createChooser(i, "Share"))
                    showMenu = false
                }
                MenuItem("Bookmarks") { showMenu = false }
                MenuItem("History") { showMenu = false }
                MenuItem("Settings") { screen = Screen.Settings; showMenu = false }
            }
        }
    }

    if (showFind) {
        AlertDialog(
            onDismissRequest = { showFind = false; activeTab?.webView?.clearMatches() },
            title = { Text("Find in page") },
            text = {
                OutlinedTextField(
                    value = findQuery,
                    onValueChange = { findQuery = it; activeTab?.webView?.findAllAsync(it) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = { showFind = false }) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { activeTab?.webView?.clearMatches(); findQuery = "" }) { Text("Clear") }
            }
        )
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

private const val ERROR_HTML = "<html><body style='background:#000;color:#fff;font-family:monospace;padding:24px'><h1>Offline</h1><p>Couldn't load this page.</p></body></html>"
