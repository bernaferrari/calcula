package org.solovyev.android.calculator

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.provider.Settings
import android.text.TextUtils
import android.util.SparseArray
import android.view.ContextThemeWrapper
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import org.solovyev.android.Check
import org.solovyev.android.calculator.functions.FunctionsActivity
import org.solovyev.android.calculator.language.Languages
import org.solovyev.android.calculator.operators.OperatorsActivity
import org.solovyev.android.calculator.preferences.PreferenceEntry
import org.solovyev.android.calculator.preferences.PreferencesActivity
import org.solovyev.android.calculator.variables.VariablesActivity
import org.solovyev.android.prefs.BooleanPreference
import org.solovyev.android.prefs.IntegerPreference
import org.solovyev.android.prefs.NumberToStringPreference
import org.solovyev.android.prefs.Preference
import org.solovyev.android.prefs.StringPreference
import org.solovyev.common.text.NumberKind
import androidx.core.content.edit

object Preferences {

    private const val HAPTIC_FEEDBACK_ENABLED_SETTING = "haptic_feedback_enabled"

    private val version = IntegerPreference.of("version", 3)

    internal fun init(application: Application, preferences: SharedPreferences) {
        val currentVersion = getVersion(preferences)
        if (currentVersion == 0) {
            preferences.edit {
                setInitialDefaultValues(application, preferences, this)
            }
        } else if (currentVersion == 1) {
            preferences.edit {
                if (!Gui.vibrateOnKeypress.isSet(preferences)) {
                    val hapticValue = Deleted.hapticFeedback.getPreference(preferences) ?: 0L
                    Gui.vibrateOnKeypress.putPreference(this, hapticValue > 0)
                }
                migratePreference(
                    preferences,
                    this,
                    Gui.keepScreenOn,
                    Deleted.preventScreenFromFading
                )
                migratePreference(preferences, this, Gui.theme, Deleted.theme)
                migratePreference(preferences, this, Gui.useBackAsPrevious, Deleted.usePrevAsBack)
                migratePreference(preferences, this, Gui.showReleaseNotes, Deleted.showReleaseNotes)
                migratePreference(preferences, this, Gui.rotateScreen, Deleted.autoOrientation)
                val layout = Deleted.layout.getPreference(preferences)
                if (TextUtils.equals(layout, "main_calculator")) {
                    Gui.mode.putPreference(this, Gui.Mode.engineer)
                } else if (TextUtils.equals(layout, "simple")) {
                    Gui.mode.putPreference(this, Gui.Mode.simple)
                } else if (!Gui.mode.isSet(preferences)) {
                    Gui.mode.putDefault(this)
                }
                version.putDefault(this)
            }
        } else if (currentVersion == 2) {
            val editor = preferences.edit()
            Gui.highContrast.tryPutDefault(preferences, editor)
            version.putDefault(editor)
            editor.apply()
        }
    }

    private fun getVersion(preferences: SharedPreferences): Int {
        return if (version.isSet(preferences)) {
            version.getPreference(preferences) ?: 0
        } else if (Deleted.appVersion.isSet(preferences)) {
            1
        } else {
            0
        }
    }

    private fun <T> migratePreference(
        preferences: SharedPreferences,
        editor: SharedPreferences.Editor,
        to: Preference<T>,
        from: Preference<T>
    ) {
        if (!to.isSet(preferences)) {
            to.putPreference(editor, from.getPreferenceNoError(preferences))
        }
    }

    private fun setInitialDefaultValues(
        application: Application,
        preferences: SharedPreferences,
        editor: SharedPreferences.Editor
    ) {
        Gui.theme.tryPutDefault(preferences, editor)
        Gui.mode.tryPutDefault(preferences, editor)
        Gui.showReleaseNotes.tryPutDefault(preferences, editor)
        Gui.useBackAsPrevious.tryPutDefault(preferences, editor)
        Gui.rotateScreen.tryPutDefault(preferences, editor)
        Gui.keepScreenOn.tryPutDefault(preferences, editor)
        Gui.language.tryPutDefault(preferences, editor)
        Gui.highContrast.tryPutDefault(preferences, editor)

        Calculations.calculateOnFly.tryPutDefault(preferences, editor)

        Onscreen.showAppIcon.tryPutDefault(preferences, editor)
        Onscreen.theme.tryPutDefault(preferences, editor)

        Widget.theme.tryPutDefault(preferences, editor)
        version.putDefault(editor)

        val cr: ContentResolver? = application.contentResolver
        if (cr != null) {
            val vibrateOnKeyPress = Settings.System.getInt(cr, HAPTIC_FEEDBACK_ENABLED_SETTING, 0) != 0
            Gui.vibrateOnKeypress.putPreference(editor, vibrateOnKeyPress)
        }
    }

    enum class SimpleTheme(
        private val appTheme: Gui.Theme?,
        val light: Boolean = false
    ) {
        default_theme(null),
        metro_blue_theme(Gui.Theme.metro_blue_theme),
        material_theme(Gui.Theme.material_theme),
        material_light_theme(Gui.Theme.material_light_theme, true);

        private val cache = mutableMapOf<Gui.Theme, SimpleTheme>()

        fun resolveThemeFor(appTheme: Gui.Theme): SimpleTheme {
            if (this == default_theme) {
                return cache.getOrPut(appTheme) { lookUpThemeFor(appTheme) }
            }
            return this
        }

        private fun lookUpThemeFor(appTheme: Gui.Theme): SimpleTheme {
            Check.isTrue(this == default_theme)
            // find direct match
            for (theme in values()) {
                if (theme.appTheme == appTheme) {
                    return theme
                }
            }

            // for metro themes return metro theme
            if (appTheme == Gui.Theme.metro_green_theme || appTheme == Gui.Theme.metro_purple_theme) {
                return metro_blue_theme
            }

            // for old themes return dark material
            return material_theme
        }

        fun getAppTheme(): Gui.Theme? {
            return appTheme
        }

        @ColorRes
        fun getDisplayTextColor(error: Boolean): Int {
            return if (error) {
                if (light) R.color.cpp_text_inverse_error else R.color.cpp_text_error
            } else {
                if (light) R.color.cpp_text_inverse else R.color.cpp_text
            }
        }
    }

    object Widget {
        val theme = StringPreference.ofEnum("widget.theme", SimpleTheme.default_theme, SimpleTheme::class.java)

        fun getTheme(preferences: SharedPreferences): SimpleTheme {
            return theme.getPreferenceNoError(preferences) ?: SimpleTheme.default_theme
        }
    }

    object Onscreen {
        val showAppIcon = BooleanPreference.of("onscreen_show_app_icon", true)
        val theme = StringPreference.ofEnum("onscreen.theme", SimpleTheme.default_theme, SimpleTheme::class.java)

        fun getTheme(preferences: SharedPreferences): SimpleTheme {
            return theme.getPreferenceNoError(preferences) ?: SimpleTheme.default_theme
        }
    }

    object Calculations {
        val calculateOnFly = BooleanPreference.of("calculations_calculate_on_fly", true)
    }

    object App

    object Gui {

        val theme = StringPreference.ofEnum("gui.theme", Theme.material_theme, Theme::class.java)
        val mode = StringPreference.ofEnum("gui.mode", Mode.simple, Mode::class.java)
        val language = StringPreference.of("gui.language", Languages.SYSTEM_LANGUAGE_CODE)
        val showReleaseNotes = BooleanPreference.of("gui.showReleaseNotes", true)
        val useBackAsPrevious = BooleanPreference.of("gui.useBackAsPrevious", false)
        val rotateScreen = BooleanPreference.of("gui.rotateScreen", true)
        val keepScreenOn = BooleanPreference.of("gui.keepScreenOn", true)
        val highContrast = BooleanPreference.of("gui.highContrast", false)
        val vibrateOnKeypress = BooleanPreference.of("gui.vibrateOnKeypress", true)

        fun getTheme(preferences: SharedPreferences): Theme {
            return theme.getPreferenceNoError(preferences) ?: Theme.material_theme
        }

        fun getMode(preferences: SharedPreferences): Mode {
            return mode.getPreferenceNoError(preferences) ?: Mode.simple
        }

        enum class Theme(
            @StringRes protected val nameRes: Int,
            @StyleRes val theme: Int,
            @StyleRes val calculatorTheme: Int,
            @StyleRes val wizardTheme: Int = R.style.Cpp_Theme_Wizard,
            @StyleRes val dialogTheme: Int = R.style.Cpp_Theme_Material_Dialog,
            @StyleRes val alertDialogTheme: Int = R.style.Cpp_Theme_Material_Dialog_Alert,
            val light: Boolean = false
        ) : PreferenceEntry {
            default_theme(R.string.cpp_theme_dark, R.style.Cpp_Theme_Gray, R.style.Cpp_Theme_Gray),
            violet_theme(R.string.cpp_theme_dark, R.style.Cpp_Theme_Violet, R.style.Cpp_Theme_Violet),
            light_blue_theme(R.string.cpp_theme_dark, R.style.Cpp_Theme_Blue, R.style.Cpp_Theme_Blue),
            metro_blue_theme(
                R.string.cpp_theme_metro_blue,
                R.style.Cpp_Theme_Metro_Blue,
                R.style.Cpp_Theme_Metro_Blue_Calculator,
                R.style.Cpp_Theme_Wizard,
                R.style.Cpp_Theme_Metro_Blue_Dialog,
                R.style.Cpp_Theme_Material_Dialog_Alert
            ),
            metro_purple_theme(
                R.string.p_metro_purple_theme,
                R.style.Cpp_Theme_Metro_Purple,
                R.style.Cpp_Theme_Metro_Purple_Calculator,
                R.style.Cpp_Theme_Wizard,
                R.style.Cpp_Theme_Metro_Purple_Dialog,
                R.style.Cpp_Theme_Material_Dialog_Alert
            ),
            metro_green_theme(
                R.string.p_metro_green_theme,
                R.style.Cpp_Theme_Metro_Green,
                R.style.Cpp_Theme_Metro_Green_Calculator,
                R.style.Cpp_Theme_Wizard,
                R.style.Cpp_Theme_Metro_Green_Dialog,
                R.style.Cpp_Theme_Material_Dialog_Alert
            ),
            material_theme(R.string.cpp_theme_dark, R.style.Cpp_Theme_Material, R.style.Cpp_Theme_Material_Calculator),
            material_black_theme(
                R.string.cpp_theme_black,
                R.style.Cpp_Theme_Material_Black,
                R.style.Cpp_Theme_Material_Black_Calculator
            ) {
                override fun getName(context: Context): String {
                    return context.getString(nameRes, material_theme.getName(context))
                }
            },
            material_you_theme(
                R.string.cpp_theme_you,
                R.style.Cpp_Theme_Material,
                R.style.Cpp_Theme_Material_Calculator
            ),
            material_light_theme(
                R.string.cpp_theme_light,
                R.style.Cpp_Theme_Material_Light,
                R.style.Cpp_Theme_Material_Light_Calculator,
                R.style.Cpp_Theme_Wizard_Light,
                R.style.Cpp_Theme_Material_Light_Dialog,
                R.style.Cpp_Theme_Material_Light_Dialog_Alert,
                true
            );

            fun getThemeFor(context: Context): Int {
                return when (context) {
                    is CalculatorActivity -> calculatorTheme
                    is FunctionsActivity.Dialog,
                    is PreferencesActivity.Dialog,
                    is VariablesActivity.Dialog,
                    is OperatorsActivity.Dialog -> dialogTheme

                    else -> theme
                }
            }

            fun getTextColorFor(context: Context): TextColor {
                val themeId = getThemeFor(context)
                var textColor = textColors.get(themeId)
                if (textColor == null) {
                    val themeContext = ContextThemeWrapper(context, themeId)
                    val a = themeContext.obtainStyledAttributes(
                        themeId,
                        intArrayOf(R.attr.cpp_text_color, R.attr.cpp_text_color_error)
                    )
                    val normal = a.getColor(0, Color.BLACK)
                    val error = a.getColor(1, Color.WHITE)
                    a.recycle()
                    textColor = TextColor(normal, error)
                    textColors.append(themeId, textColor)
                }
                return textColor
            }

            @ColorInt
            fun getScrimColorFor(context: Context): Int {
                val themeId = getThemeFor(context)
                val themeContext = ContextThemeWrapper(context, themeId)
                val a = themeContext.obtainStyledAttributes(themeId, intArrayOf(android.R.attr.background))
                val color = a.getColor(0, Color.BLACK)
                a.recycle()
                return color
            }

            override fun getName(context: Context): String {
                return context.getString(nameRes)
            }

            override val id: CharSequence
                get() = name

            companion object {
                private val textColors = SparseArray<TextColor>()
            }
        }

        enum class Mode(@StringRes val nameRes: Int) {
            engineer(R.string.cpp_mode_engineer),
            simple(R.string.cpp_mode_simple),
            modern(R.string.cpp_mode_modern)
        }

        data class TextColor(val normal: Int, val error: Int)
    }

    @Suppress("unused")
    internal object Deleted {
        val appVersion = IntegerPreference.of("application.version", IntegerPreference.DEF_VALUE)
        val feedbackWindowShown = BooleanPreference.of("feedback_window_shown", false)
        val appOpenedCounter = IntegerPreference.of("app_opened_counter", 0)
        val hapticFeedback = NumberToStringPreference.of("hapticFeedback", 60L, NumberKind.Long)
        val colorDisplay = BooleanPreference.of("org.solovyev.android.calculator.CalculatorModel_color_display", true)
        val preventScreenFromFading = BooleanPreference.of("preventScreenFromFading", true)
        val theme = StringPreference.ofEnum(
            "org.solovyev.android.calculator.CalculatorActivity_calc_theme",
            Gui.Theme.material_theme,
            Gui.Theme::class.java
        )
        val layout = StringPreference.of("org.solovyev.android.calculator.CalculatorActivity_calc_layout", "simple")
        val showReleaseNotes =
            BooleanPreference.of("org.solovyev.android.calculator.CalculatorActivity_show_release_notes", true)
        val usePrevAsBack =
            BooleanPreference.of("org.solovyev.android.calculator.CalculatorActivity_use_back_button_as_prev", false)
        val showEqualsButton = BooleanPreference.of("showEqualsButton", true)
        val autoOrientation = BooleanPreference.of("autoOrientation", true)
        val startOnBoot = BooleanPreference.of("onscreen_start_on_boot", false)
        val plotImag = BooleanPreference.of("graph_plot_imag", false)
    }
}
