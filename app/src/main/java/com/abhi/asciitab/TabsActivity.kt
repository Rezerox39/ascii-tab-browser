package com.abhi.asciitab

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TabsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        val rv = findViewById<RecyclerView>(R.id.tabGrid)
        rv.layoutManager = GridLayoutManager(this, 2)
        val adapter = TabAdapter(TabManager.tabs, { tab ->
            TabManager.activeTabId = tab.id
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }, { tab ->
            TabManager.tabs.remove(tab)
            tab.webView.destroy()
            if (TabManager.tabs.isEmpty()) {
                // ensure at least one tab
            }
            (rv.adapter as? TabAdapter)?.notifyDataSetChanged()
        })
        rv.adapter = adapter

        findViewById<android.widget.Button>(R.id.newTabBtn).setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("new_tab", true)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(i)
            finish()
        }
    }
}
