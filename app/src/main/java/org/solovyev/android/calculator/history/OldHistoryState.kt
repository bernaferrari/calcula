package org.solovyev.android.calculator.history

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import kotlinx.datetime.Clock

@Root(name = "HistoryState")
internal class OldHistoryState() {

    @field:Element
    @get:JvmName("editorStateProperty")
    var editorState: OldEditorHistoryState? = null

    @field:Element
    @get:JvmName("displayStateProperty")
    var displayState: OldDisplayHistoryState? = null

    @field:Element
    @get:JvmName("timeProperty")
    var time: Long = Clock.System.now().toEpochMilliseconds()

    @field:Element(required = false)
    @get:JvmName("commentProperty")
    var comment: String? = null

    fun getTime(): Long = time

    fun getComment(): String? = comment

    fun getEditorState(): OldEditorHistoryState {
        return editorState!!
    }

    fun getDisplayState(): OldDisplayHistoryState {
        return displayState!!
    }
}
