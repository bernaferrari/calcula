package com.bernaferrari.calculator

import android.app.Application
import com.bernaferrari.calculator.platform.AndroidPlatform

class CalculatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidPlatform.init(this)
    }
}
