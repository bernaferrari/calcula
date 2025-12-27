package org.solovyev.android.calculator.ui.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme

/**
 * Example demonstrating basic usage of CalculatorEditor component.
 *
 * This example shows:
 * - State management with EditorState
 * - Text change handling
 * - Selection change handling
 * - Basic interaction with buttons
 */
@Composable
fun CalculatorEditorBasicExample() {
    var editorState by remember { mutableStateOf(EditorState.empty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Basic Calculator Editor Example",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // The editor component
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 2.dp
        ) {
            CalculatorEditor(
                state = editorState,
                onTextChange = { newText, newSelection ->
                    // Update state with new text and cursor
                    editorState = EditorState.create(newText, newSelection)
                },
                onSelectionChange = { newSelection ->
                    // Update state with new cursor position
                    editorState = EditorState.forNewSelection(editorState, newSelection)
                }
            )
        }

        // Debug info
        Text(
            text = "Text: \"${editorState.text}\"",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Cursor Position: ${editorState.selection}",
            style = MaterialTheme.typography.bodySmall
        )

        // Example buttons
        Button(
            onClick = {
                val currentText = editorState.text.toString()
                val newText = currentText + "π"
                editorState = EditorState.create(newText, newText.length)
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Insert π")
        }

        Button(
            onClick = {
                editorState = EditorState.empty()
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Clear")
        }
    }
}

/**
 * Example demonstrating advanced usage with syntax highlighting showcase.
 */
@Composable
fun CalculatorEditorAdvancedExample() {
    var editorState by remember {
        mutableStateOf(
            EditorState.create(
                "sin(π/2) + cos(0) × ln(e) + sqrt(16) - 2^3",
                42
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Advanced Example - Syntax Highlighting",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 2.dp
        ) {
            CalculatorEditor(
                state = editorState,
                onTextChange = { newText, newSelection ->
                    editorState = EditorState.create(newText, newSelection)
                },
                onSelectionChange = { newSelection ->
                    editorState = EditorState.forNewSelection(editorState, newSelection)
                }
            )
        }

        Text(
            text = "This example showcases:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Text("• Function highlighting (sin, cos, ln, sqrt)", style = MaterialTheme.typography.bodySmall)
        Text("• Constant highlighting (π, e)", style = MaterialTheme.typography.bodySmall)
        Text("• Operator highlighting (+, ×, -, ^)", style = MaterialTheme.typography.bodySmall)
        Text("• Number highlighting", style = MaterialTheme.typography.bodySmall)
        Text("• Bracket highlighting", style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * Example demonstrating integration with a view model or controller.
 */
@Composable
fun CalculatorEditorIntegrationExample(
    // In a real app, this would be a ViewModel or similar
    onCalculate: (String) -> Unit = {}
) {
    var editorState by remember { mutableStateOf(EditorState.empty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Integration Example",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 2.dp
        ) {
            CalculatorEditor(
                state = editorState,
                onTextChange = { newText, newSelection ->
                    editorState = EditorState.create(newText, newSelection)
                    // Trigger calculation on text change
                    onCalculate(newText)
                },
                onSelectionChange = { newSelection ->
                    editorState = EditorState.forNewSelection(editorState, newSelection)
                }
            )
        }

        Text(
            text = "Integration points:",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Text("• onTextChange: Triggers calculation", style = MaterialTheme.typography.bodySmall)
        Text("• onSelectionChange: Updates cursor position", style = MaterialTheme.typography.bodySmall)
        Text("• State management: Via EditorState", style = MaterialTheme.typography.bodySmall)
    }
}

// ============================================
// Previews
// ============================================

@Preview(name = "Basic Example", showBackground = true, heightDp = 600)
@Composable
private fun PreviewBasicExample() {
    CalculatorTheme {
        CalculatorEditorBasicExample()
    }
}

@Preview(name = "Advanced Example", showBackground = true, heightDp = 600)
@Composable
private fun PreviewAdvancedExample() {
    CalculatorTheme {
        CalculatorEditorAdvancedExample()
    }
}

@Preview(name = "Integration Example", showBackground = true, heightDp = 600)
@Composable
private fun PreviewIntegrationExample() {
    CalculatorTheme {
        CalculatorEditorIntegrationExample()
    }
}

@Preview(name = "Dark Theme", showBackground = true, heightDp = 600)
@Composable
private fun PreviewDarkTheme() {
    CalculatorTheme(theme = org.solovyev.android.calculator.Preferences.Gui.Theme.material_theme) {
        CalculatorEditorBasicExample()
    }
}
