package org.solovyev.android.prefs

import android.content.SharedPreferences
import org.solovyev.common.text.Mapper
import org.solovyev.common.text.NumberKind
import org.solovyev.common.text.NumberMapper

class NumberToStringPreference<N : Number> private constructor(
    key: String,
    defaultValue: N?,
    private val mapper: Mapper<N>
) : AbstractPreference<N>(key, defaultValue) {

    override fun getPersistedValue(preferences: SharedPreferences): N? {
        return mapper.parseValue(preferences.getString(key, "0"))
    }

    override fun putPersistedValue(editor: SharedPreferences.Editor, value: N) {
        editor.putString(key, mapper.formatValue(value))
    }

    companion object {
        @JvmStatic
        fun <N : Number> of(
            key: String,
            defaultValue: N?,
            kind: NumberKind
        ): NumberToStringPreference<N> {
            return NumberToStringPreference(key, defaultValue, NumberMapper.of(kind))
        }
    }
}
