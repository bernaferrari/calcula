package org.solovyev.android.calculator.ui.compose.floating

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.ui.compose.components.CalculatorDisplay
import org.solovyev.android.calculator.ui.compose.components.CalculatorEditor
import org.solovyev.android.calculator.ui.compose.components.CalculatorKeyboard
import org.solovyev.android.calculator.ui.compose.components.LocalCalculatorHapticsEnabled
import org.solovyev.android.calculator.ui.compose.components.LocalCalculatorHighContrast
import org.solovyev.android.calculator.ui.compose.components.KeyboardActions
import org.solovyev.android.calculator.ui.compose.components.KeyboardMode

@Composable
fun FloatingCalculatorOverlay(
    displayState: DisplayState,
    editorState: EditorState,
    keyboardMode: KeyboardMode,
    highlightExpressions: Boolean,
    highContrast: Boolean,
    hapticsEnabled: Boolean,
    keyboardActions: KeyboardActions,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    onToggleFold: () -> Unit,
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    title: String,
    isFolded: Boolean,
    onDrag: (Float, Float) -> Unit,
    onHeaderHeightChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(
        LocalCalculatorHighContrast provides highContrast,
        LocalCalculatorHapticsEnabled provides hapticsEnabled
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            modifier = modifier
        ) {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                FloatingHeader(
                    isFolded = isFolded,
                    onToggleFold = onToggleFold,
                    onMinimize = onMinimize,
                    onClose = onClose,
                    title = title,
                    onDrag = onDrag,
                    onHeaderHeightChanged = onHeaderHeightChanged
                )

                if (!isFolded) {
                    FloatingCalculatorBody(
                        displayState = displayState,
                        editorState = editorState,
                        keyboardMode = keyboardMode,
                        highlightExpressions = highlightExpressions,
                        keyboardActions = keyboardActions,
                        onEditorTextChange = onEditorTextChange,
                        onEditorSelectionChange = onEditorSelectionChange
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingHeader(
    isFolded: Boolean,
    onToggleFold: () -> Unit,
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    title: String,
    onDrag: (Float, Float) -> Unit,
    onHeaderHeightChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .then(
                Modifier.onSizeChanged { onHeaderHeightChanged(it.height) }
            )
        ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggleFold, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (isFolded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            IconButton(onClick = onMinimize, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.HorizontalRule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun FloatingCalculatorBody(
    displayState: DisplayState,
    editorState: EditorState,
    keyboardMode: KeyboardMode,
    highlightExpressions: Boolean,
    keyboardActions: KeyboardActions,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(8.dp)) {
        CalculatorDisplay(
            state = displayState,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        CalculatorEditor(
            state = editorState,
            onTextChange = onEditorTextChange,
            onSelectionChange = onEditorSelectionChange,
            highlightExpressions = highlightExpressions,
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        CalculatorKeyboard(
            mode = keyboardMode,
            actions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp)
        )
    }
}
