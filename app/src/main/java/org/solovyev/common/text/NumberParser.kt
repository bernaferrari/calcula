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

import org.solovyev.android.Check

class NumberParser<N : Number> private constructor(
    private val clazz: Class<out N>
) : Parser<N> {

    @Suppress("UNCHECKED_CAST")
    override fun parseValue(value: String?): N? {
        return value?.let {
            when (clazz) {
                Integer::class.java -> it.toInt() as N
                Float::class.java -> it.toFloat() as N
                Long::class.java -> it.toLong() as N
                Double::class.java -> it.toDouble() as N
                else -> throw UnsupportedOperationException("$clazz is not supported!")
            }
        }
    }

    companion object {
        val supportedClasses: List<Class<out Number>> = listOf(
            Integer::class.java,
            Float::class.java,
            Long::class.java,
            Double::class.java
        )

        private val parsers = mutableMapOf<Class<*>, Parser<*>>().apply {
            supportedClasses.forEach { clazz ->
                put(clazz, NumberParser(clazz))
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun <N : Number> of(clazz: Class<N>): Parser<N> {
            Check.isTrue(
                supportedClasses.contains(clazz),
                "Class $clazz is not supported by ${NumberParser::class.java}"
            )
            return parsers[clazz] as Parser<N>
        }
    }
}
