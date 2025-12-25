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

import kotlin.random.Random

object Strings {

    val LINE_SEPARATOR: String = System.getProperty("line.separator")
    val EMPTY_CHARACTER_OBJECT_ARRAY: Array<Char> = emptyArray()

    // random variable for generating random strings
    private val RANDOM = Random(System.currentTimeMillis())

    fun isEmpty(s: CharSequence?): Boolean {
        return s == null || s.isEmpty()
    }

    fun getNotEmpty(s: CharSequence?, defaultValue: String): String {
        return if (isEmpty(s)) defaultValue else s.toString()
    }

    fun toObjects(array: CharArray?): Array<Char> {
        if (array == null || array.isEmpty()) {
            return EMPTY_CHARACTER_OBJECT_ARRAY
        }

        return array.toTypedArray()
    }

    fun generateRandomString(length: Int): String {
        val result = StringBuilder(length)
        for (i in 0 until length) {
            // 'A' = 65
            val ch = (Random.nextInt(52) + 65).toChar()
            result.append(ch)
        }
        return result.toString()
    }
}
