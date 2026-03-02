package org.solovyev.android.calculator.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

internal actual val PlatformUsesNativeEditorView: Boolean = false

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
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        cursorBrush = SolidColor(cursorColor),
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        maxLines = maxLines,
        interactionSource = interactionSource,
        onTextLayout = onTextLayout,
        decorationBox = decorationBox,
        visualTransformation = visualTransformation
    )
}
