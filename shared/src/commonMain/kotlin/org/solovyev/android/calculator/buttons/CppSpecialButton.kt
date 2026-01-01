package org.solovyev.android.calculator.buttons

enum class CppSpecialButton(
    val action: String,
    val glyph: Char = 0.toChar()
) {
    history("history", '\uE005'),
    history_undo("↶", '\uE007'),
    history_redo("↷", '\uE008'),
    cursor_right("▷", '\uE003'),
    cursor_to_end(">>", '\uE00B'),
    cursor_left("◁", '\uE002'),
    cursor_to_start("<<", '\uE00A'),
    settings("settings"),
    settings_widget("settings_widget"),
    like("like", '\uE006'),
    memory("memory"),
    memory_plus("M+"),
    memory_minus("M-"),
    memory_clear("MC"),
    erase("erase", '\uE004'),
    paste("paste", '\uE000'),
    copy("copy", '\uE001'),
    brackets_wrap("(…)"),
    equals("="),
    clear("clear"),
    functions("functions"),
    function_add("+ƒ"),
    var_add("+π"),
    plot_add("+plot", '\uE009'),
    open_app("open_app"),
    vars("vars"),
    operators("operators"),
    simplify("≡");

    companion object {
        private val buttonsByActions = mutableMapOf<String, CppSpecialButton>()
        private val buttonsByGlyphs = arrayOfNulls<CppSpecialButton>(200) // Assumed max glyph range size
        private const val FIRST_GLYPH = '\uE000'

        fun getByAction(action: String): CppSpecialButton? {
            if (buttonsByActions.isEmpty()) {
                initButtonsByActions()
            }
            return buttonsByActions[action]
        }

        private fun initButtonsByActions() {
            for (button in entries) {
                buttonsByActions[button.action] = button
            }
        }

        fun getByGlyph(glyph: Char): CppSpecialButton? {
            if (buttonsByGlyphs[0] == null) {
                initButtonsByGlyphs()
            }
            val position = glyphToPosition(glyph)
            if (position < 0 || position >= buttonsByGlyphs.size) {
                return null
            }
            return buttonsByGlyphs[position]
        }

        private fun glyphToPosition(glyph: Char): Int {
            return glyph - FIRST_GLYPH
        }

        private fun initButtonsByGlyphs() {
            for (button in entries) {
                if (button.glyph == 0.toChar()) {
                    continue
                }
                val position = glyphToPosition(button.glyph)
                if (position >= 0 && position < buttonsByGlyphs.size) {
                     buttonsByGlyphs[position] = button
                }
            }
        }
    }
}
