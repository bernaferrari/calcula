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
import org.solovyev.common.text.Mapper
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
        fun <N : Number> of(key: String, defaultValue: N?, clazz: Class<N>): NumberToStringPreference<N> {
            return NumberToStringPreference(key, defaultValue, NumberMapper.of(clazz))
        }
    }
}
