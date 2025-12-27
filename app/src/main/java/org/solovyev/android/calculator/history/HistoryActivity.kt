package org.solovyev.android.calculator.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ui.compose.history.HistoryComposeViewModel
import org.solovyev.android.calculator.ui.compose.history.HistoryScreen
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme

@AndroidEntryPoint
open class HistoryActivity : BaseActivity(R.string.c_history) {

    @Composable
    override fun Content() {
        val viewModel: HistoryComposeViewModel = hiltViewModel()
        val refreshTick by viewModel.refreshTick.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val themePreference by appPreferences.settings.theme.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getThemeBlocking()
        )

        CalculatorTheme(theme = themePreference) {
            val recent = remember(refreshTick) { viewModel.getRecent() }
            val saved = remember(refreshTick) { viewModel.getSaved() }
            var clearRecentDialog by remember { mutableStateOf(false) }
            var clearSavedDialog by remember { mutableStateOf(false) }
            var editState by remember { mutableStateOf<Pair<HistoryState, Boolean>?>(null) }

            HistoryScreen(
                recent = recent,
                saved = saved,
                onUse = { state ->
                    viewModel.useState(state)
                    finish()
                },
                onCopyExpression = { state ->
                    copyToClipboard(context, state.editor.getTextString())
                },
                onCopyResult = { state ->
                    copyToClipboard(context, state.display.text)
                },
                onSave = { state ->
                    editState = state to true
                },
                onEdit = { state ->
                    editState = state to false
                },
                onDelete = { state ->
                    viewModel.removeSaved(state)
                },
                onClearRecent = { clearRecentDialog = true },
                onClearSaved = { clearSavedDialog = true },
                onBack = { finish() }
            )

            if (clearRecentDialog) {
                ClearHistoryDialog(
                    onConfirm = {
                        viewModel.clearRecent()
                        clearRecentDialog = false
                    },
                    onDismiss = { clearRecentDialog = false }
                )
            }

            if (clearSavedDialog) {
                ClearHistoryDialog(
                    onConfirm = {
                        viewModel.clearSaved()
                        clearSavedDialog = false
                    },
                    onDismiss = { clearSavedDialog = false }
                )
            }

            editState?.let { (state, isNew) ->
                EditHistoryDialog(
                    state = state,
                    isNew = isNew,
                    onSave = { comment ->
                        val updated = HistoryState.builder(state, isNew)
                            .withComment(comment)
                            .build()
                        viewModel.updateSaved(updated)
                        editState = null
                    },
                    onDismiss = { editState = null }
                )
            }
        }
    }

    private fun copyToClipboard(context: Context, text: String) {
        if (text.isEmpty()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("history", text))
    }

    @Composable
    private fun EditHistoryDialog(
        state: HistoryState,
        isNew: Boolean,
        onSave: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        var comment by remember(state) { mutableStateOf(state.comment) }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = getString(R.string.cpp_edit)) },
            text = {
                Column {
                    Text(text = HistoryTextFormatter.format(state))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text(text = getString(R.string.c_history_comment)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onSave(comment) }) {
                    Text(text = getString(R.string.c_save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = getString(R.string.cpp_cancel))
                }
            }
        )
    }

    @Composable
    private fun ClearHistoryDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = getString(R.string.cpp_clear_history_title)) },
            text = { Text(text = getString(R.string.cpp_clear_history_message)) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(text = getString(R.string.cpp_clear_history))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = getString(R.string.cpp_cancel))
                }
            }
        )
    }

    class Dialog : HistoryActivity()

    companion object {
        fun getClass(context: Context): Class<out HistoryActivity> {
            return if (App.isTablet(context)) Dialog::class.java else HistoryActivity::class.java
        }
    }
}
