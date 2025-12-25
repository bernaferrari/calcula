/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.ui.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.jscl.JsclOperation
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme

/**
 * Preview parameter provider for DisplayState
 */
class DisplayStateProvider : PreviewParameterProvider<DisplayState> {
    override val values = sequenceOf(
        DisplayState.empty(),
        DisplayState.createValid(
            operation = JsclOperation.numeric,
            result = null,
            stringResult = "42",
            sequence = 1L
        ),
        DisplayState.createValid(
            operation = JsclOperation.numeric,
            result = null,
            stringResult = "3.141592653589793",
            sequence = 2L
        ),
        DisplayState.createValid(
            operation = JsclOperation.numeric,
            result = null,
            stringResult = "1234567890123456789",
            sequence = 3L
        ),
        DisplayState.createError(
            operation = JsclOperation.numeric,
            errorMessage = "Division by zero",
            sequence = 4L
        )
    )
}

/**
 * Preview parameter provider for EditorState
 */
class EditorStateProvider : PreviewParameterProvider<EditorState> {
    override val values = sequenceOf(
        EditorState.empty(),
        EditorState.create("6 × 7", 5),
        EditorState.create("sin(π/2)", 8),
        EditorState.create("2^10 + sqrt(16)", 15),
        EditorState.create("(1 + 2) × (3 + 4)", 17),
        EditorState.create("log(100) + ln(e)", 16)
    )
}

// ============================================
// Display Previews
// ============================================

@Preview(name = "Display - Empty", showBackground = true)
@Composable
private fun PreviewDisplayEmpty() {
    CalculatorTheme {
        CalculatorDisplay(
            state = DisplayState.empty(),
            onCopy = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Display - Valid Result", showBackground = true)
@Composable
private fun PreviewDisplayValid() {
    CalculatorTheme {
        CalculatorDisplay(
            state = DisplayState.createValid(
                operation = JsclOperation.numeric,
                result = null,
                stringResult = "42",
                sequence = 1L
            ),
            onCopy = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Display - Long Result", showBackground = true)
@Composable
private fun PreviewDisplayLong() {
    CalculatorTheme {
        CalculatorDisplay(
            state = DisplayState.createValid(
                operation = JsclOperation.numeric,
                result = null,
                stringResult = "3.141592653589793238462643383279",
                sequence = 2L
            ),
            onCopy = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Display - Error", showBackground = true)
@Composable
private fun PreviewDisplayError() {
    CalculatorTheme {
        CalculatorDisplay(
            state = DisplayState.createError(
                operation = JsclOperation.numeric,
                errorMessage = "Division by zero",
                sequence = 3L
            ),
            onCopy = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Display - All States", showBackground = true)
@Composable
private fun PreviewDisplayAll(
    @PreviewParameter(DisplayStateProvider::class) state: DisplayState
) {
    CalculatorTheme {
        CalculatorDisplay(
            state = state,
            onCopy = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================
// Editor Previews
// ============================================

@Preview(name = "Editor - Empty", showBackground = true)
@Composable
private fun PreviewEditorEmpty() {
    CalculatorTheme {
        CalculatorEditor(
            state = EditorState.empty(),
            onTextChange = {},
            onSelectionChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Editor - Simple Expression", showBackground = true)
@Composable
private fun PreviewEditorSimple() {
    CalculatorTheme {
        CalculatorEditor(
            state = EditorState.create("6 × 7", 5),
            onTextChange = {},
            onSelectionChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Editor - Function", showBackground = true)
@Composable
private fun PreviewEditorFunction() {
    CalculatorTheme {
        CalculatorEditor(
            state = EditorState.create("sin(π/2) + cos(0)", 17),
            onTextChange = {},
            onSelectionChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Editor - Complex Expression", showBackground = true)
@Composable
private fun PreviewEditorComplex() {
    CalculatorTheme {
        CalculatorEditor(
            state = EditorState.create("log(100) + ln(e) × sqrt(16)", 27),
            onTextChange = {},
            onSelectionChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Editor - All States", showBackground = true)
@Composable
private fun PreviewEditorAll(
    @PreviewParameter(EditorStateProvider::class) state: EditorState
) {
    CalculatorTheme {
        CalculatorEditor(
            state = state,
            onTextChange = {},
            onSelectionChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================
// Combined Previews
// ============================================

@Preview(name = "Display + Editor - Normal", showBackground = true)
@Composable
private fun PreviewCombinedNormal() {
    CalculatorTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            CalculatorDisplay(
                state = DisplayState.createValid(
                    operation = JsclOperation.numeric,
                    result = null,
                    stringResult = "42",
                    sequence = 1L
                ),
                onCopy = {},
                modifier = Modifier.fillMaxWidth()
            )
            CalculatorEditor(
                state = EditorState.create("6 × 7", 5),
                onTextChange = {},
                onSelectionChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Preview(name = "Display + Editor - Error", showBackground = true)
@Composable
private fun PreviewCombinedError() {
    CalculatorTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            CalculatorDisplay(
                state = DisplayState.createError(
                    operation = JsclOperation.numeric,
                    errorMessage = "Syntax error",
                    sequence = 1L
                ),
                onCopy = {},
                modifier = Modifier.fillMaxWidth()
            )
            CalculatorEditor(
                state = EditorState.create("2 + + 3", 5),
                onTextChange = {},
                onSelectionChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Preview(name = "Display + Editor - Long Expression", showBackground = true)
@Composable
private fun PreviewCombinedLong() {
    CalculatorTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            CalculatorDisplay(
                state = DisplayState.createValid(
                    operation = JsclOperation.numeric,
                    result = null,
                    stringResult = "3.141592653589793238462643383279",
                    sequence = 1L
                ),
                onCopy = {},
                modifier = Modifier.fillMaxWidth()
            )
            CalculatorEditor(
                state = EditorState.create("π × 1.0000000000000", 18),
                onTextChange = {},
                onSelectionChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

// ============================================
// Full Screen Preview
// ============================================

@Preview(name = "Calculator Screen", showBackground = true, heightDp = 800)
@Composable
private fun PreviewCalculatorScreen() {
    CalculatorTheme {
        CalculatorScreenPreview()
    }
}

@Preview(name = "Calculator Screen - Dark", showBackground = true, heightDp = 800)
@Composable
private fun PreviewCalculatorScreenDark() {
    CalculatorTheme(darkTheme = true) {
        CalculatorScreenPreview()
    }
}
