package com.example.labirynt

import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        val switchSwap = findViewById<Switch>(R.id.switchSwapAxes)
        switchSwap.isChecked = prefs.getBoolean("swap_axes", false)

        switchSwap.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("swap_axes", isChecked).apply()
        }
    }
}
