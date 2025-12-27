package org.solovyev.android.prefs

import android.content.SharedPreferences

class IntegerPreference private constructor(
    key: String,
    defaultValue: Int?
) : AbstractPreference<Int>(key, defaultValue) {

    override fun getPersistedValue(preferences: SharedPreferences): Int {
        return preferences.getInt(key, DEF_VALUE)
    }

    override fun putPersistedValue(editor: SharedPreferences.Editor, value: Int) {
        editor.putInt(key, value)
    }

    companion object {
        const val DEF_VALUE = -1

        @JvmStatic
        fun of(key: String, defaultValue: Int?): IntegerPreference {
            return IntegerPreference(key, defaultValue)
        }
    }
}
