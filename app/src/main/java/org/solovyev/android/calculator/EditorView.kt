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

package org.solovyev.android.calculator

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.ContextMenu
import org.solovyev.android.Check
import org.solovyev.android.calculator.view.EditTextCompat
import org.solovyev.android.views.Adjuster
import org.solovyev.android.calculator.core.Editor as CoreEditor

class EditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : EditTextCompat(context, attrs, defStyle) {

    private var editorChange = false
    private var editor: Editor? = null
    private var coreEditor: CoreEditor? = null

    fun setEditor(editor: Editor?) {
        if (this.editor == editor) {
            return
        }
        if (editor != null) {
            // avoid losing cursor position on focus restore. First request focus, then set cursor
            // position. Consequent requestFocus() should be no-op
            requestFocus()
            setState(editor.state)
        }
        // update editor at the end to avoid side-effects of #requestFocus() and #setState()
        this.editor = editor
        this.coreEditor = null
    }

    fun setEditor(editor: CoreEditor?) {
        if (this.coreEditor == editor) {
            return
        }
        if (editor != null) {
            // avoid losing cursor position on focus restore. First request focus, then set cursor
            // position. Consequent requestFocus() should be no-op
            requestFocus()
            setState(editor.state)
        }
        // update editor at the end to avoid side-effects of #requestFocus() and #setState()
        this.coreEditor = editor
        this.editor = null
    }

    init {
        init()
    }

    private fun init() {
        if (!App.isFloatingCalculator(context)) {
            Adjuster.adjustText(
                this,
                0.22f,
                resources.getDimensionPixelSize(R.dimen.cpp_min_editor_text_size)
            )
        }
        addTextChangedListener(MyTextWatcher())
        dontShowSoftInputOnFocusCompat()
        // the state is controlled by Editor
        isSaveEnabled = false
    }

    override fun onCreateContextMenu(menu: ContextMenu?) {
        super.onCreateContextMenu(menu)
        menu?.removeItem(android.R.id.selectAll)
    }

    fun setState(state: EditorState) {
        Check.isMainThread()
        // we don't want to be notified about changes we make ourselves
        editorChange = true
        if (App.getTheme().light && App.isFloatingCalculator(context)) {
            // don't need formatting
            setText(state.getTextString())
        } else {
            setText(state.text, BufferType.EDITABLE)
        }
        editorChange = false
        // Use CoreEditor or Editor clamp - both have the same signature
        val clampValue = if (coreEditor != null) {
            CoreEditor.clamp(state.selection, length())
        } else {
            Editor.clamp(state.selection, length())
        }
        setSelection(clampValue)
    }

    override fun onSelectionChanged(start: Int, end: Int) {
        Check.isMainThread()
        super.onSelectionChanged(start, end)
        if (start != end) {
            return
        }
        if ((editor == null && coreEditor == null) || editorChange) {
            return
        }
        editor?.setSelection(start)
        coreEditor?.setSelection(start)
    }

    override fun getAutofillType(): Int = AUTOFILL_TYPE_NONE

    private inner class MyTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if ((editor == null && coreEditor == null) || editorChange) {
                return
            }
            editor?.setText(s.toString())
            coreEditor?.setText(s.toString())
        }
    }
}
