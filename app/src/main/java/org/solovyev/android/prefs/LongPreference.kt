package org.solovyev.android.prefs

import android.content.SharedPreferences

class LongPreference private constructor(
    key: String,
    defaultValue: Long?
) : AbstractPreference<Long>(key, defaultValue) {

    override fun getPersistedValue(preferences: SharedPreferences): Long {
        return preferences.getLong(key, -1)
    }

    override fun putPersistedValue(editor: SharedPreferences.Editor, value: Long) {
        editor.putLong(key, value)
    }

    companion object {
        @JvmStatic
        fun of(key: String, defaultValue: Long?): LongPreference {
            return LongPreference(key, defaultValue)
        }
    }
}
