package com.abhi.asciitab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhi.asciitab.ui.theme.AsciiTabTheme
import com.abhi.asciitab.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AsciiTabTheme {
                val vm: BrowserViewModel = viewModel()
                val tabs by vm.tabs.collectAsState()
                val activeId by vm.activeTabId.collectAsState()
                BrowserApp(vm, tabs, activeId)
            }
        }
    }
}
