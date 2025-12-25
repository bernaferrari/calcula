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
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.util.TypedValue
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.solovyev.android.Check
import org.solovyev.android.calculator.view.TextHighlighter
import org.solovyev.android.views.AutoResizeTextView

class DisplayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AutoResizeTextView(context, attrs, defStyle) {

    private var engine: Engine? = null
    private var textHighlighter: TextHighlighter? = null
    var state: DisplayState = DisplayState.empty()
        private set
    private var highlighterJob: Job? = null
    private val coroutineScope: CoroutineScope?
        get() = findViewTreeLifecycleOwner()?.lifecycleScope

    init {
        init(context)
    }

    private fun getTextHighlighter(): TextHighlighter? {
        if (textHighlighter == null && engine != null) {
            textHighlighter = TextHighlighter(textColors.defaultColor, false, engine!!)
        }
        return textHighlighter
    }

    private fun init(context: Context) {
        setAddEllipsis(false)
        setMinTextSize(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            10f,
            resources.displayMetrics
        ))
        // make text scrollable if it doesn't fit
        movementMethod = ScrollingMovementMethod.getInstance()
    }

    private val textColor: Preferences.Gui.TextColor
        get() = App.getThemeFor(context).getTextColorFor(context)

    fun setState(newState: DisplayState) {
        Check.isMainThread()

        state = newState
        if (state.valid) {
            asyncHighlightText(newState)
        } else {
            cancelAsyncHighlightText(applyLastState = false)
            setText(App.unspan(text))
            setTextColor(textColor.error)
        }
    }

    private fun cancelAsyncHighlightText(applyLastState: Boolean) {
        highlighterJob?.cancel()
        highlighterJob = null
    }

    private fun asyncHighlightText(state: DisplayState) {
        cancelAsyncHighlightText(applyLastState = false)

        val highlighter = getTextHighlighter()
        val stateText = state.text

        if (TextUtils.isEmpty(stateText) || highlighter == null) {
            setText(stateText)
            setTextColor(textColor.normal)
            return
        }

        val scope = coroutineScope
        if (scope == null) {
            // Fallback to synchronous processing if no lifecycle owner
            try {
                val result = highlighter.process(stateText).getCharSequence()
                setText(result)
                setTextColor(textColor.normal)
            } catch (e: ParseException) {
                setText(stateText)
                setTextColor(textColor.normal)
            }
            return
        }

        highlighterJob = scope.launch {
            val highlightedText = withContext(Dispatchers.Default) {
                try {
                    highlighter.process(stateText).getCharSequence()
                } catch (e: ParseException) {
                    stateText as CharSequence
                }
            }

            setText(highlightedText)
            setTextColor(textColor.normal)
        }
    }

    fun setEngine(engine: Engine?) {
        this.engine = engine
    }

    fun onDestroy() {
        highlighterJob?.cancel()
        highlighterJob = null
    }
}
