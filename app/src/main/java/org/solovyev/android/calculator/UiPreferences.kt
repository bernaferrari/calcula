package org.solovyev.android.calculator

import android.content.SharedPreferences
import org.solovyev.android.prefs.BooleanPreference
import org.solovyev.android.prefs.IntegerPreference
import org.solovyev.android.prefs.Preference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UiPreferences @Inject constructor() {

    @set:JvmName("showFixableErrorDialogProperty")
    var showFixableErrorDialog: Boolean = true

    fun isShowFixableErrorDialog(): Boolean = showFixableErrorDialog

    fun setShowFixableErrorDialog(showFixableErrorDialog: Boolean) {
        this.showFixableErrorDialog = showFixableErrorDialog
    }

    object Converter {
        val lastDimension: Preference<Int> = IntegerPreference.of("converter.lastDimension", -1)
        val lastUnitsFrom: Preference<Int> = IntegerPreference.of("converter.lastUnitsFrom", -1)
        val lastUnitsTo: Preference<Int> = IntegerPreference.of("converter.lastUnitsTo", -1)
    }

    companion object {
        val opened: Preference<Int> = IntegerPreference.of("opened", 0)
        val version: Preference<Int> = IntegerPreference.of("version", 1)
        val appVersion: Preference<Int> = IntegerPreference.of("appVersion", IntegerPreference.DEF_VALUE)
        val rateUsShown: Preference<Boolean> = BooleanPreference.of("rateUsShown", false)

        @JvmStatic
        fun init(preferences: SharedPreferences, uiPreferences: SharedPreferences) {
            val currentVersion = getVersion(uiPreferences)
            if (currentVersion == 0) {
                val editor = uiPreferences.edit()
                migratePreference(
                    uiPreferences,
                    preferences,
                    editor,
                    rateUsShown,
                    Preferences.Deleted.feedbackWindowShown
                )
                migratePreference(
                    uiPreferences,
                    preferences,
                    editor,
                    opened,
                    Preferences.Deleted.appOpenedCounter
                )
                migratePreference(
                    uiPreferences,
                    preferences,
                    editor,
                    appVersion,
                    Preferences.Deleted.appVersion
                )
                version.putDefault(editor)
                editor.apply()
            }
        }

        private fun <T> migratePreference(
            uiPreferences: SharedPreferences,
            preferences: SharedPreferences,
            uiEditor: SharedPreferences.Editor,
            uiPreference: Preference<T>,
            preference: Preference<T>
        ) {
            if (!preference.isSet(preferences)) {
                return
            }
            if (!uiPreference.isSet(uiPreferences)) {
                uiPreference.putPreference(uiEditor, preference.getPreferenceNoError(preferences))
            }
        }

        private fun getVersion(uiPreferences: SharedPreferences): Int {
            return if (version.isSet(uiPreferences)) {
                version.getPreference(uiPreferences) ?: 0
            } else {
                0
            }
        }
    }
}
