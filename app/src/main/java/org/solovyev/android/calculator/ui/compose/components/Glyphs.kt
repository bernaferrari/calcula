package org.solovyev.android.calculator.ui.compose.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun glyphString(@StringRes id: Int): String {
    return normalizeGlyphString(stringResource(id))
}

internal fun normalizeGlyphString(value: String): String {
    return if (value.length >= 2 && value.first() == '"' && value.last() == '"') {
        value.substring(1, value.length - 1)
    } else {
        value
    }
}
