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
import org.solovyev.android.prefs.CachingMapper

class NumberMapper<N : Number> private constructor(
    private val parser: Parser<out N>,
    private val formatter: Formatter<N>
) : Mapper<N> {

    private constructor(clazz: Class<out N>) : this(
        NumberParser.of(clazz),
        ValueOfFormatter.getNotNullFormatter()
    )

    override fun formatValue(value: N?): String? = formatter.formatValue(value)

    override fun parseValue(value: String?): N? = parser.parseValue(value)

    companion object {
        private val supportedClasses = NumberParser.supportedClasses

        private val mappers = mutableMapOf<Class<*>, Mapper<*>>().apply {
            supportedClasses.forEach { clazz ->
                put(clazz, CachingMapper.of(newInstance(clazz)))
            }
        }

        fun <N : Number> newInstance(
            parser: Parser<out N>,
            formatter: Formatter<N>
        ): Mapper<N> = NumberMapper(parser, formatter)

        private fun <N : Number> newInstance(clazz: Class<out N>): Mapper<N> =
            NumberMapper(clazz)

        @Suppress("UNCHECKED_CAST")
        fun <N : Number> of(clazz: Class<out N>): Mapper<N> {
            Check.isTrue(
                supportedClasses.contains(clazz),
                "Class $clazz is not supported by ${NumberMapper::class.java}"
            )
            return mappers[clazz] as Mapper<N>
        }
    }
}
