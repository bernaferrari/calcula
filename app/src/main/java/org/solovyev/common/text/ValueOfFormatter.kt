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
 * ---------------------------------------------------------------------
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.common.text

class ValueOfFormatter<T> private constructor(
    private val processNulls: Boolean
) : Formatter<T> {

    override fun formatValue(value: T?): String? =
        when {
            value == null && processNulls -> value.toString()
            value == null -> null
            else -> value.toString()
        }

    companion object {
        private val notNullFormatter = ValueOfFormatter<Any>(processNulls = false)
        private val nullableFormatter = ValueOfFormatter<Any>(processNulls = true)

        @Suppress("UNCHECKED_CAST")
        fun <T> getNotNullFormatter(): ValueOfFormatter<T> = notNullFormatter as ValueOfFormatter<T>

        @Suppress("UNCHECKED_CAST")
        fun <T> getNullableFormatter(): ValueOfFormatter<T> = nullableFormatter as ValueOfFormatter<T>
    }
}
