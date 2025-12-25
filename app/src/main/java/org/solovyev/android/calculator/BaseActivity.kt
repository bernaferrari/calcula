package org.solovyev.android.calculator

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Insets
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.Check
import org.solovyev.android.calculator.ga.Ga
import org.solovyev.android.calculator.language.Language
import org.solovyev.android.calculator.language.Languages
import org.solovyev.android.calculator.view.Tabs
import org.solovyev.android.views.dragbutton.DirectionDragImageButton
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity(
    @LayoutRes private val layoutId: Int,
    @StringRes private val titleId: Int
) : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    constructor(@StringRes titleId: Int) : this(R.layout.activity_tabs, titleId)

    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var languages: Languages

    @Inject
    lateinit var editor: Editor

    @Inject
    lateinit var calculator: Calculator

    @Inject
    lateinit var ga: Lazy<Ga>

    @Inject
    lateinit var typeface: Typeface

    protected val tabs = Tabs(this)
    protected lateinit var mainView: ViewGroup
    protected var toolbar: Toolbar? = null
    protected var fab: FloatingActionButton? = null

    private var theme = Preferences.Gui.Theme.material_theme
    private var mode = Preferences.Gui.Mode.engineer
    private var language: Language = Languages.SYSTEM_LANGUAGE
    private var paused = true

    val activityMode: Preferences.Gui.Mode
        get() = mode

    override fun onCreate(savedInstanceState: Bundle?) {
        onPreCreate()
        super.onCreate(savedInstanceState)

        languages.updateContextLocale(this, false)

        createView()

        updateOrientation()
        updateKeepScreenOn()

        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun createView() {
        setContentView(layoutId)
        val contentView = findViewById<View>(android.R.id.content)
        mainView = contentView.findViewById(R.id.main)
        toolbar = contentView.findViewById(R.id.toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mainView.setOnApplyWindowInsetsListener { v, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val lp = v.layoutParams as ViewGroup.MarginLayoutParams
                lp.topMargin = insets.top
                lp.bottomMargin = insets.bottom
                v.layoutParams = lp
                WindowInsets.CONSUMED
            }
        }

        fab = contentView.findViewById(R.id.fab)
        bindViews(contentView)

        // title must be updated as if a non-system language is used the value from AndroidManifest
        // might be cached
        if (titleId != 0) {
            setTitle(titleId)
        }

        fixFonts(mainView, typeface)
        initToolbar()
        populateTabs(tabs)
        tabs.onCreate()
    }

    protected open fun bindViews(contentView: View) {
        // Override in subclasses
    }

    private fun initToolbar() {
        toolbar?.let {
            if (this !is CalculatorActivity) {
                setSupportActionBar(it)
                supportActionBar?.apply {
                    Check.isNotNull(this)
                    setDisplayHomeAsUpEnabled(true)
                }

                if (App.isTablet(this)) {
                    val lp = it.layoutParams
                    if (lp is AppBarLayout.LayoutParams) {
                        lp.scrollFlags = 0
                        it.layoutParams = lp
                    }
                }
            }
        }
    }

    private fun onPreCreate() {
        theme = Preferences.Gui.getTheme(preferences)
        setTheme(theme.getThemeFor(this))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val scrimColor = theme.getScrimColorFor(this)
            enableEdgeToEdge(
                if (theme.light) {
                    SystemBarStyle.light(scrimColor, scrimColor)
                } else {
                    SystemBarStyle.dark(scrimColor)
                }
            )
        }

        mode = Preferences.Gui.getMode(preferences)
        language = languages.getCurrent()
    }

    protected open fun populateTabs(tabs: Tabs) {
        // Override in subclasses
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU && event?.repeatCount == 0) {
            return toggleMenu()
        }
        return super.onKeyUp(keyCode, event)
    }

    protected open fun toggleMenu(): Boolean {
        toolbar?.let {
            if (it.isOverflowMenuShowing) {
                it.hideOverflowMenu()
            } else {
                it.showOverflowMenu()
            }
            return true
        }
        return false
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
        tabs.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {
        key?.let {
            when {
                Preferences.Gui.rotateScreen.isSameKey(it) -> updateOrientation()
                Preferences.Gui.keepScreenOn.isSameKey(it) -> updateKeepScreenOn()
            }

            if (paused) {
                return
            }

            when {
                Preferences.Gui.theme.isSameKey(it) -> restartIfThemeChanged()
                Preferences.Gui.language.isSameKey(it) -> restartIfLanguageChanged()
            }
        }
    }

    fun restartIfModeChanged(): Boolean {
        val newMode = Preferences.Gui.mode.getPreference(preferences)
        if (newMode != mode) {
            App.restartActivity(this)
            return true
        }
        return false
    }

    fun restartIfThemeChanged(): Boolean {
        val newTheme = Preferences.Gui.theme.getPreferenceNoError(preferences) ?: return false
        val themeId = theme.getThemeFor(this)
        val newThemeId = newTheme.getThemeFor(this)
        if (themeId != newThemeId) {
            App.restartActivity(this)
            return true
        }
        return false
    }

    fun restartIfLanguageChanged(): Boolean {
        val current = languages.getCurrent()
        if (current != language) {
            App.restartActivity(this)
            return true
        }
        return false
    }

    private fun updateOrientation() {
        requestedOrientation = if (Preferences.Gui.rotateScreen.getPreference(preferences) == true) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun updateKeepScreenOn() {
        window?.let {
            if (Preferences.Gui.keepScreenOn.getPreference(preferences) == true) {
                it.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                it.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    fun withFab(@DrawableRes icon: Int, listener: View.OnClickListener) {
        fab?.let {
            it.visibility = View.VISIBLE
            it.setImageResource(icon)
            it.setOnClickListener(listener)
        } ?: Check.shouldNotHappen()
    }

    companion object {
        fun setFont(view: View, newTypeface: Typeface) {
            when (view) {
                is TextView -> {
                    val oldTypeface = view.typeface
                    if (oldTypeface != null && oldTypeface == newTypeface) {
                        return
                    }
                    val style = oldTypeface?.style ?: Typeface.NORMAL
                    view.setTypeface(newTypeface, style)
                }
                is DirectionDragImageButton -> {
                    view.setTypeface(newTypeface)
                }
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
