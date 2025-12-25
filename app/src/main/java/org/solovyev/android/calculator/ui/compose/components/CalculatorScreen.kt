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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState

/**
 * Main calculator screen combining display, editor, and keyboard.
 *
 * Layout:
 * - Display at top (shows result)
 * - Editor below display (shows input)
 * - Keyboard at bottom (for input)
 *
 * @param displayState Current display state
 * @param editorState Current editor state
 * @param onCopyResult Callback when copy button is clicked
 * @param onEditorTextChange Callback when editor text changes
 * @param onEditorSelectionChange Callback when editor selection changes
 * @param modifier Modifier to be applied to the screen
 */
@Composable
fun CalculatorScreen(
    displayState: DisplayState,
    editorState: EditorState,
    onCopyResult: () -> Unit,
    onEditorTextChange: (String) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Display section (result)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp, max = 120.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                CalculatorDisplay(
                    state = displayState,
                    onCopy = onCopyResult,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Editor section (input)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 150.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                )
            ) {
                CalculatorEditor(
                    state = editorState,
                    onTextChange = onEditorTextChange,
                    onSelectionChange = onEditorSelectionChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Keyboard section (buttons)
            // TODO: Implement Compose keyboard
            // For now, this is a placeholder. The keyboard needs to be injected
            // from the activity/fragment that has access to the dependency graph.
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = MaterialTheme.colorScheme.background
            ) {
                // Placeholder for keyboard
                // In a real implementation, the keyboard view would be passed as a parameter
                // or accessed through a different mechanism that doesn't require direct instantiation
            }
        }
    }
}

/**
 * Preview-friendly calculator screen with sample states
 */
@Composable
fun CalculatorScreenPreview(
    modifier: Modifier = Modifier
) {
    var displayState by remember {
        mutableStateOf(
            DisplayState.createValid(
                operation = org.solovyev.android.calculator.jscl.JsclOperation.numeric,
                result = null,
                stringResult = "42",
                sequence = 1L
            )
        )
    }

    var editorState by remember {
        mutableStateOf(
            EditorState.create(
                text = "6 × 7",
                selection = 5
            )
        )
    }

    CalculatorScreen(
        displayState = displayState,
        editorState = editorState,
        onCopyResult = { /* Handle copy */ },
        onEditorTextChange = { newText ->
            editorState = EditorState.create(newText, newText.length)
        },
        onEditorSelectionChange = { newSelection ->
            editorState = EditorState.forNewSelection(editorState, newSelection)
        },
        modifier = modifier
    )
}
