package org.solovyev.android.calculator

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class EditorState(
    val sequence: Long,
    val text: String,
    val selection: Int
) {

    private constructor(text: CharSequence, selection: Int) : this(
        sequence = Calculator.nextSequence(),
        text = text.toString(),
        selection = selection
    )

    fun getTextString(): String = text

    fun same(that: EditorState): Boolean {
        return text == that.text && selection == that.selection
    }

    fun isEmpty(): Boolean {
        return text.isEmpty()
    }

    override fun toString(): String {
        return "EditorState{sequence=$sequence, text=$text, selection=$selection}"
    }

    companion object {
        
        fun empty(): EditorState {
            return EditorState(Calculator.NO_SEQUENCE, "", 0)
        }

        
        fun forNewSelection(state: EditorState, selection: Int): EditorState {
            return state.copy(selection = selection)
        }

        
        fun create(text: CharSequence, selection: Int): EditorState {
            return EditorState(text, selection)
        }
    }
}
