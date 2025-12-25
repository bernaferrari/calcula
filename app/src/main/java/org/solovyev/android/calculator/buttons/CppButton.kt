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

package org.solovyev.android.calculator.buttons

import android.util.SparseArray
import org.solovyev.android.Check
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.buttons.CppSpecialButton.*

enum class CppButton(
    val id: Int,
    val action: String,
    val actionLong: String? = null
) {
    // digits
    one(R.id.cpp_button_1, "1"),
    two(R.id.cpp_button_2, "2"),
    three(R.id.cpp_button_3, "3"),
    four(R.id.cpp_button_4, "4"),
    five(R.id.cpp_button_5, "5"),
    six(R.id.cpp_button_6, "6"),
    seven(R.id.cpp_button_7, "7"),
    eight(R.id.cpp_button_8, "8"),
    nine(R.id.cpp_button_9, "9"),
    zero(R.id.cpp_button_0, "0"),

    period(R.id.cpp_button_period, "."),
    brackets(R.id.cpp_button_round_brackets, "()"),

    memory(R.id.cpp_button_memory, CppSpecialButton.memory.action),
    settings(R.id.cpp_button_settings, CppSpecialButton.settings.action),
    settings_widget(R.id.cpp_button_settings_widget, CppSpecialButton.settings_widget.action),
    like(R.id.cpp_button_like, CppSpecialButton.like.action),

    // last row
    left(R.id.cpp_button_left, cursor_left.action),
    right(R.id.cpp_button_right, cursor_right.action),
    vars(R.id.cpp_button_vars, CppSpecialButton.vars.action),
    functions(R.id.cpp_button_functions, CppSpecialButton.functions.action),
    operators(R.id.cpp_button_operators, CppSpecialButton.operators.action),
    app(R.id.cpp_button_app, open_app.action),
    history(R.id.cpp_button_history, CppSpecialButton.history.action),

    // operations
    multiplication(R.id.cpp_button_multiplication, "×"),
    division(R.id.cpp_button_division, "/"),
    plus(R.id.cpp_button_plus, "+"),
    subtraction(R.id.cpp_button_subtraction, "−"),
    percent(R.id.cpp_button_percent, "%"),
    power(R.id.cpp_button_power, "^"),

    // last column
    clear(R.id.cpp_button_clear, CppSpecialButton.clear.action),
    erase(R.id.cpp_button_erase, CppSpecialButton.erase.action, CppSpecialButton.clear.action),
    copy(R.id.cpp_button_copy, CppSpecialButton.copy.action),
    paste(R.id.cpp_button_paste, CppSpecialButton.paste.action),

    // equals
    equals(R.id.cpp_button_equals, CppSpecialButton.equals.action);

    companion object {
        private val buttonsByIds = SparseArray<CppButton>()

        fun getById(buttonId: Int): CppButton? {
            initButtonsByIdsMap()
            return buttonsByIds.get(buttonId)
        }

        private fun initButtonsByIdsMap() {
            Check.isMainThread()
            if (buttonsByIds.size() != 0) {
                return
            }
            for (button in values()) {
                buttonsByIds.append(button.id, button)
            }
        }
    }
}
