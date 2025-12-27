package org.solovyev.android.prefs

import android.content.SharedPreferences

/**
 * Base class for [Preference] implementation. Contains preference key and default value
 *
 * @param T type of preference
 */
abstract class AbstractPreference<T>(
    override val key: String,
    override val defaultValue: T?
) : Preference<T> {

    final override fun getPreference(preferences: SharedPreferences): T? {
        return if (isSet(preferences)) {
            getPersistedValue(preferences)
        } else {
            defaultValue
        }
    }

    override fun getPreferenceNoError(preferences: SharedPreferences): T? {
        return if (isSet(preferences)) {
            try {
                getPersistedValue(preferences)
            } catch (e: RuntimeException) {
                defaultValue
            }
        } else {
            defaultValue
        }
    }

    override fun putDefault(editor: SharedPreferences.Editor) {
        putPreference(editor, defaultValue)
    }

    override fun putDefault(preferences: SharedPreferences) {
        putPreference(preferences, defaultValue)
    }

    override fun putPreference(editor: SharedPreferences.Editor, value: T?) {
        value?.let { putPersistedValue(editor, it) }
    }

    override fun putPreference(preferences: SharedPreferences, value: T?) {
        value?.let {
            preferences.edit().apply {
                putPersistedValue(this, it)
                apply()
            }
        }
    }

    override fun isSet(preferences: SharedPreferences): Boolean {
        return preferences.contains(key)
    }

    final override fun tryPutDefault(preferences: SharedPreferences): Boolean {
        return preferences.edit().let { editor ->
            val changed = tryPutDefault(preferences, editor)
            editor.apply()
            changed
        }
    }

    final override fun tryPutDefault(
        preferences: SharedPreferences,
        editor: SharedPreferences.Editor
    ): Boolean {
        return if (isSet(preferences)) {
            false
        } else {
            putDefault(editor)
            true
        }
    }

    final override fun isSameKey(key: String): Boolean {
        return this.key == key
    }

    /**
     * @param preferences preferences container
     * @return preference value from preferences with key defined by [key] property
     */
    protected abstract fun getPersistedValue(preferences: SharedPreferences): T?

    /**
     * Method saved preference to preferences container editor
     *
     * @param editor editor in which value must be saved
     * @param value  value to be saved
     */
    protected abstract fun putPersistedValue(editor: SharedPreferences.Editor, value: T)
}
