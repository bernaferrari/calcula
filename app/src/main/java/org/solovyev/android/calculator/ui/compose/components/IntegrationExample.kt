package org.solovyev.android.calculator.ui.compose.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme

/**
 * Example ViewModel for managing calculator state.
 *
 * In a real implementation, this would interface with:
 * - Calculator engine for evaluating expressions
 * - Editor for managing input state
 * - Display for managing result state
 */
class CalculatorViewModel : ViewModel() {

    private val _displayState = MutableStateFlow(DisplayState.empty())
    val displayState: StateFlow<DisplayState> = _displayState.asStateFlow()

    private val _editorState = MutableStateFlow(EditorState.empty())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    /**
     * Called when the user changes the input text
     */
    fun onTextChanged(newText: String) {
        val currentState = _editorState.value
        _editorState.value = EditorState.create(
            text = newText,
            selection = newText.length
        )

        // In a real implementation, you would:
        // 1. Update the editor via the Editor component
        // 2. Trigger calculation if needed
        // 3. Update display state with result or error

        // Example: Calculate if expression is complete
        // evaluateExpression(newText)
    }

    /**
     * Called when the user moves the cursor
     */
    fun onSelectionChanged(newSelection: Int) {
        val currentState = _editorState.value
        _editorState.value = EditorState.forNewSelection(
            state = currentState,
            selection = newSelection
        )

        // In a real implementation, you would:
        // Update the editor's cursor position
    }

    /**
     * Called when the user requests to copy the result
     */
    fun onCopyResult(context: Context) {
        val result = _displayState.value.text
        if (result.isNotEmpty() && _displayState.value.valid) {
            copyToClipboard(context, result)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    fun onEquals() {
        // Trigger evaluation
    }

    fun onSimplify() {
        // Trigger simplify
    }

    fun onPlot() {
        // Trigger plotting
    }

    /**
     * Example method to evaluate an expression
     * In a real implementation, this would use the Calculator engine
     */
    private fun evaluateExpression(expression: String) {
        try {
            // Simulate calculation
            // val result = calculator.evaluate(expression)
            // _displayState.value = DisplayState.createValid(...)
        } catch (e: Exception) {
            // _displayState.value = DisplayState.createError(...)
        }
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Calculator result", text)
        clipboard.setPrimaryClip(clip)
    }
}

/**
 * Example Activity showing how to use the Compose components.
 *
 * This demonstrates:
 * 1. Setting up the ViewModel
 * 2. Collecting state as Compose State
 * 3. Passing callbacks to the UI
 * 4. Applying the theme
 */
class CalculatorComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CalculatorTheme {
                CalculatorContent()
            }
        }
    }
}

/**
 * Main content composable
 */
@Composable
private fun CalculatorContent(
    viewModel: CalculatorViewModel = viewModel()
) {
    val displayState by viewModel.displayState.collectAsState()
    val editorState by viewModel.editorState.collectAsState()

    CalculatorScreen(
        displayState = displayState,
        editorState = editorState,
        onEditorTextChange = { newText, newSelection ->
            viewModel.onTextChanged(newText)
            viewModel.onSelectionChanged(newSelection)
        },
        onEditorSelectionChange = { newSelection ->
            viewModel.onSelectionChanged(newSelection)
        },
        keyboard = { modifier ->
            Surface(modifier = modifier) {}
        },
        onEquals = { viewModel.onEquals() },
        onSimplify = { viewModel.onSimplify() },
        onPlot = { viewModel.onPlot() },
        overlayContent = {}
    )
}

/**
 * Example integration with existing BaseActivity.
 * Shows how to migrate from View-based to Compose gradually.
 */
abstract class BaseComposeActivity : ComponentActivity() {

    /**
     * Helper method to set up Compose content with the calculator theme
     */
    protected fun setCalculatorContent(content: @Composable () -> Unit) {
        setContent {
            CalculatorTheme {
                content()
            }
        }
    }
}

/**
 * Example showing how to use individual components separately
 */
@Composable
fun IndividualComponentsExample() {
    val displayState = DisplayState.createValid(
        operation = org.solovyev.android.calculator.jscl.JsclOperation.numeric,
        result = null,
        stringResult = "42",
        sequence = 1L
    )

    val editorState = EditorState.create(
        text = "6 × 7",
        selection = 5
    )

    // Use just the display
    CalculatorDisplay(
        state = displayState
    )

    // Or use just the editor
    CalculatorEditor(
        state = editorState,
        onTextChange = { _, _ -> /* handle text change */ },
        onSelectionChange = { /* handle selection change */ }
    )

    // Or use the full screen
    CalculatorScreen(
        displayState = displayState,
        editorState = editorState,
        onEditorTextChange = { _, _ -> /* handle text change */ },
        onEditorSelectionChange = { /* handle selection change */ },
        keyboard = { modifier ->
            Surface(modifier = modifier) {}
        },
        onEquals = { /* handle equals */ },
        onSimplify = { /* handle simplify */ },
        onPlot = { /* handle plot */ },
        overlayContent = {}
    )
}

/**
 * Example showing state management patterns
 */
@Composable
fun StateManagementExample() {
    // Using remember for local state
    val localDisplayState by remember { mutableStateOf(DisplayState.empty()) }

    // Using ViewModel for shared state
    val viewModel: CalculatorViewModel = viewModel()
    val displayState by viewModel.displayState.collectAsState()

    // Using State hoisting
    var localEditorState by remember {
        mutableStateOf(EditorState.empty())
    }

    CalculatorEditor(
        state = localEditorState,
        onTextChange = { newText, newSelection ->
            localEditorState = EditorState.create(newText, newSelection)
        },
        onSelectionChange = { newSelection ->
            localEditorState = EditorState.forNewSelection(localEditorState, newSelection)
        }
    )
}
