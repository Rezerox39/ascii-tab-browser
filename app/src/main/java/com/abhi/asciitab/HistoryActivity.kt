package com.abhi.asciitab

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        title = "History"
        val list = findViewById<ListView>(R.id.listView)
        val prefs = getSharedPreferences("history", MODE_PRIVATE)
        val urls = prefs.all.keys.toList()
        list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, urls)
        list.setOnItemClickListener { _, _, pos, _ ->
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("open_url", urls[pos])
            startActivity(i)
            finish()
        }
    }
}
