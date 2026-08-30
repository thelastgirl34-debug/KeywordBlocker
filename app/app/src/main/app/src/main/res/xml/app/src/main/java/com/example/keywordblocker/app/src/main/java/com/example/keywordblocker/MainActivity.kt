package com.example.keywordblocker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(60, 60, 60, 60)
        }

        val text = TextView(this).apply {
            text = "Keyword Blocker\n\nKorumayı başlatmak için aşağıdaki butona basıp Erişilebilirlik iznini verin."
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 60)
        }

        val button = Button(this).apply {
            text = "Erişilebilirlik İznini Aç"
            textSize = 16f
            setOnClickListener {
                try {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } catch (e: Exception) {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
        }

        layout.addView(text)
        layout.addView(button)
        setContentView(layout)
    }
}
