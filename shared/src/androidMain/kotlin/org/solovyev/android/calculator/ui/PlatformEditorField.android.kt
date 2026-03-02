package org.solovyev.android.calculator.ui

import android.content.Context
import android.graphics.Color.TRANSPARENT
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.viewinterop.AndroidView

internal actual val PlatformUsesNativeEditorView: Boolean = true

private class SelectionAwareEditText(context: Context) : AppCompatEditText(context) {
    var suppressCallbacks: Boolean = false
    var onSelectionChangedCallback: ((Int, Int) -> Unit)? = null

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (!suppressCallbacks) {
            onSelectionChangedCallback?.invoke(selStart, selEnd)
        }
    }
}

private fun SelectionAwareEditText.applyCaretColor(colorArgb: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        textCursorDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(colorArgb)
            setSize(2, 1)
        }
        return
    }

    // Best-effort cursor tint for pre-Android 10 using reflection.
    runCatching {
        val textViewClass = TextView::class.java
        val cursorDrawableResField = textViewClass.getDeclaredField("mCursorDrawableRes").apply {
            isAccessible = true
        }
        val cursorDrawableRes = cursorDrawableResField.getInt(this)
        if (cursorDrawableRes == 0) return

        val editorField = textViewClass.getDeclaredField("mEditor").apply { isAccessible = true }
        val editor = editorField.get(this) ?: return
        val editorClass = editor.javaClass
        val cursorDrawableField = editorClass.getDeclaredField("mCursorDrawable").apply {
            isAccessible = true
        }

        val drawable = context.getDrawable(cursorDrawableRes)?.mutate() ?: return
        drawable.setTint(colorArgb)
        cursorDrawableField.set(editor, arrayOf(drawable, drawable))
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
internal actual fun PlatformEditorField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    readOnly: Boolean,
    textStyle: TextStyle,
    cursorColor: Color,
    keyboardOptions: KeyboardOptions,
    singleLine: Boolean,
    maxLines: Int,
    interactionSource: MutableInteractionSource,
    onTextLayout: (TextLayoutResult) -> Unit,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit,
    visualTransformation: VisualTransformation
) {
    val currentOnValueChange = rememberUpdatedState(onValueChange)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val editText = SelectionAwareEditText(context).apply {
                setBackgroundColor(TRANSPARENT)
                includeFontPadding = false
                setPadding(0, 0, 0, 0)
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                isHorizontalScrollBarEnabled = true
                setHorizontallyScrolling(singleLine)
                setShowSoftInputOnFocus(false)
                isLongClickable = true
                setTextIsSelectable(true)

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                    override fun afterTextChanged(s: Editable?) {
                        if (suppressCallbacks) return
                        val text = s?.toString().orEmpty()
                        val selection = selectionStart.coerceIn(0, text.length)
                        currentOnValueChange.value(TextFieldValue(text = text, selection = TextRange(selection)))
                    }
                })

                onSelectionChangedCallback = { start, end ->
                    if (start == end && !suppressCallbacks) {
                        val text = text?.toString().orEmpty()
                        val selection = start.coerceIn(0, text.length)
                        currentOnValueChange.value(
                            TextFieldValue(
                                text = text,
                                selection = TextRange(selection)
                            )
                        )
                    }
                }
            }
            editText
        },
        update = { editText ->
            editText.suppressCallbacks = true
            val targetText = value.text
            if (editText.text?.toString() != targetText) {
                editText.setText(targetText)
            }
            val targetSelection = value.selection.end.coerceIn(0, targetText.length)
            if (editText.selectionStart != targetSelection || editText.selectionEnd != targetSelection) {
                editText.setSelection(targetSelection)
            }

            editText.isEnabled = enabled
            editText.isFocusable = enabled && !readOnly
            editText.isFocusableInTouchMode = enabled && !readOnly

            editText.maxLines = maxLines
            editText.setSingleLine(singleLine)
            editText.setHorizontallyScrolling(singleLine)
            editText.inputType = if (singleLine) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            } else {
                InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            }

            editText.setTextColor(textStyle.color.toArgb())
            editText.textSize = if (textStyle.fontSize.isSp) textStyle.fontSize.value else 16f
            editText.letterSpacing = if (textStyle.letterSpacing.isSp && editText.textSize != 0f) {
                textStyle.letterSpacing.value / editText.textSize
            } else {
                0f
            }
            editText.applyCaretColor(cursorColor.toArgb())

            editText.suppressCallbacks = false
        }
    )
}
