package com.abhi.asciitab

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        val searchSpinner = findViewById<Spinner>(R.id.spinnerSearch)
        val uaSpinner = findViewById<Spinner>(R.id.spinnerUA)
        val textSpinner = findViewById<Spinner>(R.id.spinnerText)

        val searchOptions = arrayOf("Google", "Bing", "DuckDuckGo")
        val uaOptions = arrayOf("Mobile", "Desktop")
        val textOptions = arrayOf("Small", "Medium", "Large")

        searchSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, searchOptions)
        uaSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, uaOptions)
        textSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, textOptions)

        searchSpinner.setSelection(prefs.getInt("searchEngine", 0))
        uaSpinner.setSelection(if (prefs.getBoolean("desktopUA", false)) 1 else 0)
        textSpinner.setSelection(prefs.getInt("textSize", 1))

        val switches = mapOf(
            R.id.switchAdblock to "adblock",
            R.id.switchJs to "js",
            R.id.switchNight to "night",
            R.id.switchDesktop to "desktopMode",
            R.id.switchCookies to "cookies",
            R.id.switchLocation to "location",
            R.id.switchDataSaver to "dataSaver"
        )
        val defaults = mapOf(
            "adblock" to true, "js" to true, "night" to true,
            "desktopMode" to false, "cookies" to true, "location" to false, "dataSaver" to false
        )
        for ((id, key) in switches) {
            val sw = findViewById<Switch>(id)
            sw.isChecked = prefs.getBoolean(key, defaults[key] ?: true)
            sw.setOnCheckedChangeListener { _, v -> prefs.edit().putBoolean(key, v).apply() }
        }

        searchSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                prefs.edit().putInt("searchEngine", pos).apply()
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
        uaSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                prefs.edit().putBoolean("desktopUA", pos == 1).apply()
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
        textSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                prefs.edit().putInt("textSize", pos).apply()
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }
}
