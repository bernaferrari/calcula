package org.solovyev.android.prefs

import android.content.SharedPreferences

class FloatPreference private constructor(
    key: String,
    defaultValue: Float?
) : AbstractPreference<Float>(key, defaultValue) {

    override fun getPersistedValue(preferences: SharedPreferences): Float {
        return preferences.getFloat(key, -1f)
    }

    override fun putPersistedValue(editor: SharedPreferences.Editor, value: Float) {
        editor.putFloat(key, value)
    }

    companion object {
        @JvmStatic
        fun of(key: String, defaultValue: Float?): FloatPreference {
            return FloatPreference(key, defaultValue)
        }
    }
}
