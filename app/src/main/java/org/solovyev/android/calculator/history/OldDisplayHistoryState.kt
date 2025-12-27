package org.solovyev.android.calculator.history

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import org.solovyev.android.calculator.jscl.JsclOperation

@Root(name = "DisplayHistoryState")
internal class OldDisplayHistoryState() : Cloneable {

    @field:Element
    @get:JvmName("editorStateProperty")
    var editorState: OldEditorHistoryState? = null

    @field:Element
    @get:JvmName("jsclOperationProperty")
    var jsclOperation: JsclOperation? = null

    fun getEditorState(): OldEditorHistoryState {
        return editorState!!
    }

    fun getJsclOperation(): JsclOperation {
        return jsclOperation!!
    }
}
