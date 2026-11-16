package com.abhi.asciitab

import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val adblock = findViewById<Switch>(R.id.switchAdblock)
        val js = findViewById<Switch>(R.id.switchJs)
        val night = findViewById<Switch>(R.id.switchNight)

        adblock.isChecked = prefs.getBoolean("adblock", true)
        js.isChecked = prefs.getBoolean("js", true)
        night.isChecked = prefs.getBoolean("night", true)

        adblock.setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean("adblock", v).apply() }
        js.setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean("js", v).apply() }
        night.setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean("night", v).apply() }
    }
}
