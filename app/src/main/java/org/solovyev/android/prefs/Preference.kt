package org.solovyev.android.prefs

import android.content.SharedPreferences

/**
 * Class for working with android preferences: can save and load preferences, convert them to custom java objects
 * and use default value;
 *
 * @param T type of java object preference
 */
interface Preference<T> {

    /**
     * Method returns key of preference used in android: the key with which current preference is saved in persistence
     *
     * @return android preference key
     */
    val key: String

    /**
     * @return default preference value, may be null
     */
    val defaultValue: T?

    /**
     * NOTE: this method can throw runtime exceptions if errors occurred while extracting preferences values
     *
     * @param preferences application preferences
     * @return value from preference, default value if no value in preference was found
     */
    fun getPreference(preferences: SharedPreferences): T?

    /**
     * NOTE: this method SHOULD not throw any runtime exceptions BUT return default value if any error occurred
     *
     * @param preferences application preferences
     * @return value from preference, default value if no value in preference was found or error occurred
     */
    fun getPreferenceNoError(preferences: SharedPreferences): T?

    /**
     * Method puts (saves) preference represented by [value] in [editor] container
     */
    fun putPreference(editor: SharedPreferences.Editor, value: T?)
    
    fun putPreference(preferences: SharedPreferences, value: T?)

    /**
     * Method saves default value in preferences container.
     * Should behave exactly as `p.putPreference(preferences, p.defaultValue)`
     *
     * @param editor preferences editor
     */
    fun putDefault(editor: SharedPreferences.Editor)
    
    fun putDefault(preferences: SharedPreferences)

    /**
     * @param preferences preferences container
     * @return true if any value is saved in preferences container, false - otherwise
     */
    fun isSet(preferences: SharedPreferences): Boolean

    /**
     * Method applies default value to preference only if explicit value is not set
     *
     * @param preferences preferences
     * @param editor preferences editor
     * @return true if default values have been applied, false otherwise
     */
    fun tryPutDefault(preferences: SharedPreferences, editor: SharedPreferences.Editor): Boolean
    
    fun tryPutDefault(preferences: SharedPreferences): Boolean

    /**
     * @param key preference key
     * @return true if current preferences has the same key
     */
    fun isSameKey(key: String): Boolean
}
