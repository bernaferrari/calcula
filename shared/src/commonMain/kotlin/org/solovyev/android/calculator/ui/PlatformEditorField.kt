package org.solovyev.android.calculator.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation

internal expect val PlatformUsesNativeEditorView: Boolean

@Composable
internal expect fun PlatformEditorField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle,
    cursorColor: Color,
    keyboardOptions: KeyboardOptions,
    singleLine: Boolean,
    maxLines: Int,
    interactionSource: MutableInteractionSource,
    onTextLayout: (TextLayoutResult) -> Unit,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit,
    visualTransformation: VisualTransformation
)
