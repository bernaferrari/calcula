package org.solovyev.android.calculator

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.language.Language
import org.solovyev.android.calculator.language.Languages
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity(
    @StringRes private val titleId: Int
) : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var languages: Languages

    @Inject
    lateinit var calculator: Calculator

    private var theme = Preferences.Gui.Theme.material_theme
    private var mode = Preferences.Gui.Mode.engineer
    private var language: Language = Languages.SYSTEM_LANGUAGE
    private var paused = true

    val activityMode: Preferences.Gui.Mode
        get() = mode

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Languages.wrapContext(newBase, App.appPreferences))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onPreCreate()

        if (titleId != 0) {
            // setTitle(titleId) // Removed as optional for Compose
        }

        observeSettings()

        setContent {
            Content()
        }
    }

    @Composable
    protected abstract fun Content()

    private fun onPreCreate() {
        theme = appPreferences.settings.getThemeBlocking()
        setTheme(theme.getThemeFor(this))

        val scrimColor = theme.getScrimColorFor(this)
        enableEdgeToEdge(
            if (theme.light) {
                SystemBarStyle.light(scrimColor, scrimColor)
            } else {
                SystemBarStyle.dark(scrimColor)
            }
        )

        mode = appPreferences.settings.getModeBlocking()
        language = languages.get(appPreferences.settings.getLanguageBlocking())
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyUp(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        paused = false
        if (!restartIfThemeChanged()) {
            restartIfLanguageChanged()
        }
    }

    override fun onPause() {
        paused = true
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun restartIfModeChanged(): Boolean {
        val newMode = appPreferences.settings.getModeBlocking()
        if (newMode != mode) {
            App.restartActivity(this)
            return true
        }
        return false
    }

    fun restartIfThemeChanged(): Boolean {
        val newTheme = appPreferences.settings.getThemeBlocking()
        val themeId = theme.getThemeFor(this)
        val newThemeId = newTheme.getThemeFor(this)
        if (themeId != newThemeId) {
            App.restartActivity(this)
            return true
        }
        return false
    }

    fun restartIfLanguageChanged(): Boolean {
        val current = languages.get(appPreferences.settings.getLanguageBlocking())
        if (current != language) {
            App.restartActivity(this)
            return true
        }
        return false
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
        updateOrientation(appPreferences.settings.getRotateScreenBlocking())
        updateKeepScreenOn(appPreferences.settings.getKeepScreenOnBlocking())

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    appPreferences.settings.rotateScreen.collect { enabled ->
                        updateOrientation(enabled)
                    }
                }
                launch {
                    appPreferences.settings.keepScreenOn.collect { enabled ->
                        updateKeepScreenOn(enabled)
                    }
                }
                launch {
                    appPreferences.settings.theme.collect { newTheme ->
                        if (paused) return@collect
                        if (newTheme != theme) {
                            theme = newTheme
                            restartIfThemeChanged()
                        }
                    }
                }
                launch {
                    appPreferences.settings.language.collect { newLanguage ->
                        if (paused) return@collect
                        val resolved = languages.get(newLanguage)
                        if (resolved != language) {
                            language = resolved
                            restartIfLanguageChanged()
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

