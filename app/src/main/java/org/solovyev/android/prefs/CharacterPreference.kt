package org.solovyev.android.prefs

import android.content.SharedPreferences

class CharacterPreference private constructor(
    key: String,
    defaultValue: Char?
) : AbstractPreference<Char>(key, defaultValue) {

    override fun getPersistedValue(preferences: SharedPreferences): Char {
        return preferences.getInt(key, 0).toChar()
    }

    override fun putPersistedValue(editor: SharedPreferences.Editor, value: Char) {
        editor.putInt(key, value.code)
    }

    companion object {
        @JvmStatic
        fun of(key: String, defaultValue: Char?): CharacterPreference {
            return CharacterPreference(key, defaultValue)
        }
    }
}
