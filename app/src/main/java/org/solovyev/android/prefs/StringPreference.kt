package org.solovyev.android.prefs

import android.content.SharedPreferences
import org.solovyev.common.text.EnumMapper
import org.solovyev.common.text.Mapper
import org.solovyev.common.text.StringMapper

/**
 * [Preference] implementation which uses [String] way of storing object in persistence.
 * This class provides methods for mapping real java objects to String and vice versa.
 *
 * @param T
 */
class StringPreference<T> constructor(
    key: String,
    defaultValue: T?,
    mapper: Mapper<T>
) : AbstractPreference<T>(key, defaultValue) {

    private val mapper: Mapper<T> = CachingMapper.of(mapper)

    override fun getPersistedValue(preferences: SharedPreferences): T? {
        return mapper.parseValue(preferences.getString(key, null))
    }

    override fun putPersistedValue(editor: SharedPreferences.Editor, value: T) {
        editor.putString(key, mapper.formatValue(value))
    }

    companion object {
        @JvmStatic
        fun of(key: String, defaultValue: String?): StringPreference<String> {
            return StringPreference(key, defaultValue, StringMapper)
        }

        @JvmStatic
        fun <T> ofTypedValue(key: String, defaultValue: String?, mapper: Mapper<T>): StringPreference<T> {
            return StringPreference(key, mapper.parseValue(defaultValue), mapper)
        }

        @JvmStatic
        fun <T> ofTypedValue(key: String, defaultValue: T?, mapper: Mapper<T>): StringPreference<T> {
            return StringPreference(key, defaultValue, mapper)
        }

        @JvmStatic
        fun <T : Enum<T>> ofEnum(key: String, defaultValue: T?, enumType: Class<T>): StringPreference<T> {
            return StringPreference(key, defaultValue, EnumMapper.of(enumType))
        }
    }
}
