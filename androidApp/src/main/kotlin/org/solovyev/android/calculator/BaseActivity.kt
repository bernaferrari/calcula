package org.solovyev.android.calculator

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.solovyev.android.calculator.language.AndroidLanguages
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

abstract class BaseActivity(
    private val titleId: Int
) : ComponentActivity() {

    protected val appPreferences: AppPreferences by inject()
    private var paused = true
    private var lastTheme: String = ""
    private var lastMode: String = ""
    private var lastLanguage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            lastTheme = appPreferences.gui.theme.first()
            lastMode = appPreferences.gui.mode.first()
            lastLanguage = appPreferences.gui.language.first()
            updateOrientation(appPreferences.gui.rotateScreen.first())
            updateKeepScreenOn(appPreferences.gui.keepScreenOn.first())
            observeSettings()
        }

        setContent {
            Content()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        // Apply preferred locale before view inflation.
        val wrapped = try {
            AndroidLanguages.wrapContext(newBase, appPreferences)
        } catch (_: Throwable) {
            newBase
        }
        super.attachBaseContext(wrapped)
    }

    @Composable
    protected abstract fun Content()

    override fun onResume() {
        super.onResume()
        paused = false
    }

    override fun onPause() {
        paused = true
        super.onPause()
    }

    private fun updateOrientation(enabled: Boolean) {
        requestedOrientation = if (enabled) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun updateKeepScreenOn(enabled: Boolean) {
        window?.let {
            if (enabled) {
                it.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                it.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    private fun observeSettings() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    appPreferences.gui.rotateScreen.collect { enabled ->
                        updateOrientation(enabled)
                    }
                }
                launch {
                    appPreferences.gui.keepScreenOn.collect { enabled ->
                        updateKeepScreenOn(enabled)
                    }
                }
                launch {
                    appPreferences.gui.mode.collect { newMode ->
                        if (paused) return@collect
                        if (newMode != lastMode) {
                            lastMode = newMode
                            recreate()
                        }
                    }
                }
                launch {
                    appPreferences.gui.theme.collect { newTheme ->
                        if (paused) return@collect
                        if (newTheme != lastTheme) {
                            lastTheme = newTheme
                            recreate()
                        }
                    }
                }
                launch {
                    appPreferences.gui.language.collect { newLanguage ->
                        if (paused) return@collect
                        if (newLanguage != lastLanguage) {
                            lastLanguage = newLanguage
                            recreate()
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun setFont(view: View, newTypeface: Typeface) {
            if (view is TextView) {
                val oldTypeface = view.typeface
                if (oldTypeface != null && oldTypeface == newTypeface) {
                    return
                }
                val style = oldTypeface?.style ?: Typeface.NORMAL
                view.setTypeface(newTypeface, style)
            }
        }

        fun fixFonts(view: View, typeface: Typeface) {
            if (view is ViewGroup) {
                for (index in 0 until view.childCount) {
                    fixFonts(view.getChildAt(index), typeface)
                }
            } else {
                setFont(view, typeface)
            }
        }
    }
}
