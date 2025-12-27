package org.solovyev.android.prefs

import android.content.SharedPreferences

class BooleanPreference private constructor(
    key: String,
    defaultValue: Boolean?
) : AbstractPreference<Boolean>(key, defaultValue) {

    override fun getPersistedValue(preferences: SharedPreferences): Boolean {
        return preferences.getBoolean(key, false)
    }

    override fun putPersistedValue(editor: SharedPreferences.Editor, value: Boolean) {
        editor.putBoolean(key, value)
    }

    companion object {
        @JvmStatic
        fun of(key: String, defaultValue: Boolean?): BooleanPreference {
            return BooleanPreference(key, defaultValue)
        }
    }
}
