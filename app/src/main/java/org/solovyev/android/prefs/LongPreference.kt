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
