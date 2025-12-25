/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

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
