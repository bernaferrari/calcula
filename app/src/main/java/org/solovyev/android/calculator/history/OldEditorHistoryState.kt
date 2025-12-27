package org.solovyev.android.calculator.history

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import org.solovyev.android.calculator.EditorState

@Root(name = "EditorHistoryState")
internal class OldEditorHistoryState() : Cloneable {

    @field:Element
    @get:JvmName("cursorPositionProperty")
    var cursorPosition: Int = 0

    @field:Element(required = false)
    @get:JvmName("textProperty")
    var text: String? = ""

    fun getText(): String? = text

    fun getCursorPosition(): Int = cursorPosition

    companion object {
        @JvmStatic
        fun create(state: EditorState): OldEditorHistoryState {
            return OldEditorHistoryState().apply {
                text = state.getTextString()
                cursorPosition = state.selection
            }
        }
    }
}
