package org.solovyev.android.calculator.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.solovyev.android.calculator.DisplayState

internal data class DisplayResultResolution(
    val text: String,
    val isCachedValue: Boolean,
    val updatedLastValidText: String
)

internal fun resolveDisplayResult(
    state: DisplayState,
    editorText: String,
    lastValidText: String
): DisplayResultResolution {
    val updatedLastValidText = when {
        state.valid && state.text.isNotBlank() -> state.text
        state.valid && state.text.isBlank() && editorText.isBlank() -> ""
        else -> lastValidText
    }

    return when {
        state.valid && state.text.isNotBlank() -> DisplayResultResolution(
            text = state.text,
            isCachedValue = false,
            updatedLastValidText = updatedLastValidText
        )
        state.valid && state.text.isBlank() && editorText.isNotBlank() && updatedLastValidText.isNotBlank() -> DisplayResultResolution(
            text = updatedLastValidText,
            isCachedValue = true,
            updatedLastValidText = updatedLastValidText
        )
        !state.valid && updatedLastValidText.isNotBlank() -> DisplayResultResolution(
            text = updatedLastValidText,
            isCachedValue = true,
            updatedLastValidText = updatedLastValidText
        )
        else -> DisplayResultResolution(
            text = state.text,
            isCachedValue = false,
            updatedLastValidText = updatedLastValidText
        )
    }
}

@Composable
internal fun rememberResolvedDisplayResult(
    state: DisplayState,
    editorText: String
): DisplayResultResolution {
    var lastValidText by remember { mutableStateOf("") }
    val resolution = resolveDisplayResult(
        state = state,
        editorText = editorText,
        lastValidText = lastValidText
    )

    LaunchedEffect(resolution.updatedLastValidText) {
        if (resolution.updatedLastValidText != lastValidText) {
            lastValidText = resolution.updatedLastValidText
        }
    }

    return resolution
}
