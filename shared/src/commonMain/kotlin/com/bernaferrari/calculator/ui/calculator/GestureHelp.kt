package com.bernaferrari.calculator.ui.calculator

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bernaferrari.calculator.resources.*
import org.jetbrains.compose.resources.stringResource

/** Available from Settings as well as the first-run hint; scrolls with large system fonts. */
@Composable
fun GestureHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.gesture_help_title)) },
        text = { Text(stringResource(Res.string.gesture_help_body), modifier = Modifier.verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.gesture_help_done)) } },
    )
}
