package org.solovyev.android.calculator.buttons

import org.solovyev.android.calculator.buttons.CppSpecialButton.*

enum class CppButton(
    val action: String,
    val actionLong: String? = null
) {
    // digits
    one("1"),
    two("2"),
    three("3"),
    four("4"),
    five("5"),
    six("6"),
    seven("7"),
    eight("8"),
    nine("9"),
    zero("0"),

    period("."),
    brackets("()"),

    memory(CppSpecialButton.memory.action),
    settings(CppSpecialButton.settings.action),
    settings_widget(CppSpecialButton.settings_widget.action),
    like(CppSpecialButton.like.action),

    // last row
    left(cursor_left.action),
    right(cursor_right.action),
    vars(CppSpecialButton.vars.action),
    functions(CppSpecialButton.functions.action),
    operators(CppSpecialButton.operators.action),
    app(open_app.action),
    history(CppSpecialButton.history.action),

    // operations
    multiplication("×"),
    division("/"),
    plus("+"),
    subtraction("−"),
    percent("%"),
    power("^"),

    // last column
    clear(CppSpecialButton.clear.action),
    erase(CppSpecialButton.erase.action, CppSpecialButton.clear.action),
    copy(CppSpecialButton.copy.action),
    paste(CppSpecialButton.paste.action),

    // equals
    equals(CppSpecialButton.equals.action);

    companion object {
        private val buttonsByAction = mutableMapOf<String, CppButton>()

        fun getByAction(action: String): CppButton? {
            if (buttonsByAction.isEmpty()) {
                initButtonsMap()
            }
            return buttonsByAction[action]
        }

        private fun initButtonsMap() {
            for (button in entries) {
                buttonsByAction[button.action] = button
            }
        }
    }
}
