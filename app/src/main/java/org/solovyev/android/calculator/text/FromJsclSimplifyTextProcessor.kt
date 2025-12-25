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

package org.solovyev.android.calculator.text

import jscl.math.Generic
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.math.MathType

class FromJsclSimplifyTextProcessor(private val engine: Engine) : TextProcessor<String, Generic> {

    private val mathTypes = listOf(MathType.function, MathType.constant)

    override fun process(from: Generic): String = fixMultiplicationSigns(from.toString())

    fun process(s: String): String = fixMultiplicationSigns(s)

    private fun fixMultiplicationSigns(s: String): String = buildString {
        val results = MathType.Results()

        var mathTypeBefore: MathType.Result? = null
        var mathType: MathType.Result? = null
        var mathTypeAfter: MathType.Result? = null

        var i = 0
        while (i < s.length) {
            results.release(mathTypeBefore)
            mathTypeBefore = mathType

            mathType = mathTypeAfter ?: MathType.getType(s, i, false, results.obtain(), engine)

            val ch = s[i]
            if (ch == '*') {
                mathTypeAfter = if (i + 1 < s.length) {
                    MathType.getType(s, i + 1, false, results.obtain(), engine)
                } else {
                    null
                }

                if (needMultiplicationSign(mathTypeBefore?.type, mathTypeAfter?.type)) {
                    append(engine.multiplicationSign)
                }
            } else {
                when (mathType.type) {
                    MathType.constant, MathType.function, MathType.operator -> {
                        append(mathType.match)
                        i += mathType.match.length - 1
                    }
                    else -> append(ch)
                }
                mathTypeAfter = null
            }
            i++
        }
    }

    private fun needMultiplicationSign(mathTypeBefore: MathType?, mathTypeAfter: MathType?): Boolean {
        if (mathTypeBefore == null || mathTypeAfter == null) {
            return true
        }

        return when {
            mathTypeBefore in mathTypes || mathTypeAfter in mathTypes -> false
            mathTypeBefore == MathType.close_group_symbol -> false
            mathTypeAfter == MathType.open_group_symbol -> false
            else -> true
        }
    }
}
