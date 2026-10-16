package com.bernaferrari.calculator

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Fully transparent bars, even with 3-button navigation: the keypad already pads itself
        // above them, so a scrim would only draw a band across the bottom.
        val transparent = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = transparent, navigationBarStyle = transparent)
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false
        window.colorMode = ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT
        setContent { App() }
    }
}
