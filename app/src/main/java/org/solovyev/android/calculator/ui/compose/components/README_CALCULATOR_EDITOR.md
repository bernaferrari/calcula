# CalculatorEditor Component

A production-ready Jetpack Compose component for displaying and editing mathematical expressions with syntax highlighting, cursor management, and text selection support.

## Features

- **Syntax Highlighting**: Automatically highlights different token types:
  - Numbers (including decimals and scientific notation) in secondary color
  - Operators (+, -, ×, ÷, etc.) in tertiary color with bold weight
  - Functions (sin, cos, log, etc.) in primary color with semi-bold weight
  - Constants (π, e, i, etc.) in secondary color with bold italic style
  - Brackets and parentheses in surface variant color

- **Blinking Cursor**: Smooth cursor animation at the current selection position
  - Shows when editor is empty
  - Built-in cursor provided by `BasicTextField` when text is present
  - 530ms blink interval (matches system standard)

- **Text Selection**: Full text selection support with Material3 theming
  - Custom selection handle colors matching primary theme color
  - Semi-transparent selection background (30% opacity)

- **Auto-Scrolling**: Automatically scrolls to keep the cursor visible
  - Smooth animated scrolling
  - Maintains right-to-left alignment for calculator-style input

- **Responsive Font Sizing**: Text size automatically adjusts based on expression length
  - Configurable min/max font sizes
  - Smooth size transitions as text length changes

- **Material3 Integration**: Fully integrated with Material3 theming
  - Uses color scheme from theme
  - Respects dark/light mode
  - Consistent with other Material3 components

## Usage

### Basic Example

```kotlin
import androidx.compose.runtime.*
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.ui.compose.components.CalculatorEditor

@Composable
fun MyCalculatorScreen() {
    var editorState by remember { mutableStateOf(EditorState.empty()) }

    CalculatorEditor(
        state = editorState,
        onTextChange = { newText ->
            editorState = EditorState.create(newText, newText.length)
        },
        onSelectionChange = { newSelection ->
            editorState = EditorState.forNewSelection(editorState, newSelection)
        }
    )
}
```

### Advanced Example with Custom Sizing

```kotlin
import androidx.compose.ui.unit.sp

@Composable
fun CustomSizedEditor() {
    var editorState by remember { mutableStateOf(EditorState.empty()) }

    CalculatorEditor(
        state = editorState,
        onTextChange = { newText ->
            editorState = EditorState.create(newText, newText.length)
        },
        onSelectionChange = { newSelection ->
            editorState = EditorState.forNewSelection(editorState, newSelection)
        },
        minTextSize = 20.sp,  // Minimum size for very long expressions
        maxTextSize = 40.sp   // Maximum size for short expressions
    )
}
```

### Integration with Existing Editor Class

```kotlin
import javax.inject.Inject
import org.solovyev.android.calculator.Editor

@Composable
fun IntegratedEditor(editor: Editor) {
    val editorState by editor.stateFlow.collectAsState()

    CalculatorEditor(
        state = editorState,
        onTextChange = { newText ->
            editor.setText(newText)
        },
        onSelectionChange = { newSelection ->
            editor.setSelection(newSelection)
        }
    )
}
```

## Component API

### Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `state` | `EditorState` | Yes | - | Current editor state (text and cursor position) |
| `onTextChange` | `(String) -> Unit` | Yes | - | Callback invoked when text changes |
| `onSelectionChange` | `(Int) -> Unit` | Yes | - | Callback invoked when cursor position changes |
| `modifier` | `Modifier` | No | `Modifier` | Modifier to apply to the component |
| `minTextSize` | `TextUnit` | No | `24.sp` | Minimum font size for long expressions |
| `maxTextSize` | `TextUnit` | No | `36.sp` | Maximum font size for short expressions |

### EditorState

The `EditorState` class represents the current state of the editor:

```kotlin
data class EditorState(
    val sequence: Long,      // Unique sequence number for state changes
    val text: CharSequence,  // Current text content
    val selection: Int       // Cursor position (0-based)
)
```

Common operations:

```kotlin
// Create empty state
val empty = EditorState.empty()

// Create state with text
val state = EditorState.create("2 + 2", 5)

// Change selection
val newState = EditorState.forNewSelection(state, 3)
```

## Syntax Highlighting Rules

### Supported Mathematical Functions

**Trigonometric**: sin, cos, tan, cot, sec, csc, asin, acos, atan, etc.

**Hyperbolic**: sinh, cosh, tanh, asinh, acosh, atanh, etc.

**Logarithmic**: ln, log, lg, log10, log2, exp

**Root Functions**: sqrt, cbrt, root

**Rounding**: ceil, floor, round, trunc

**Other**: abs, sgn, max, min, gcd, lcm, factorial, mod, deg, rad

### Supported Constants

- **π** or **pi**: Pi constant
- **e**: Euler's number
- **i** or **j**: Imaginary unit
- **inf** or **infinity**: Infinity
- **nan**: Not a number

### Supported Operators

Basic: `+`, `-`, `*`, `/`, `÷`, `×`, `·`

Advanced: `^`, `%`, `=`, `!`, `<`, `>`, `≤`, `≥`, `≠`, `∧`, `∨`, `⊕`

## Implementation Details

### Visual Transformation

The component uses `visualTransformation` to apply syntax highlighting without modifying the actual text value. This ensures:

- Clean separation between display and data
- No impact on text processing or calculations
- Efficient rendering without text manipulation

### Cursor Management

The blinking cursor is implemented using:

- `InfiniteTransition` for continuous animation
- `RepeatMode.Reverse` for smooth blinking
- Custom `BlinkingCursor` composable for empty state
- Native `BasicTextField` cursor for non-empty state

### Text Selection

Text selection is fully supported through:

- `CompositionLocalProvider` for custom selection colors
- `TextSelectionColors` matching Material3 theme
- Native selection handles from `BasicTextField`

## Performance Considerations

- **Efficient Syntax Highlighting**: Uses visual transformation instead of rebuilding annotated strings
- **Derivable State**: Font size calculation uses `derivedStateOf` to minimize recompositions
- **Scroll Optimization**: Auto-scrolling includes a small delay to ensure layout completion
- **State Management**: Uses `LaunchedEffect` with proper keys to avoid unnecessary updates

## Accessibility

The component inherits accessibility features from `BasicTextField`:

- Screen reader support
- Text selection announcements
- Cursor position announcements
- Semantic properties from Material3

## Testing

Preview composables are available in `Previews.kt`:

```kotlin
@Preview
@Composable
fun PreviewEditorEmpty() {
    CalculatorTheme {
        CalculatorEditor(
            state = EditorState.empty(),
            onTextChange = {},
            onSelectionChange = {}
        )
    }
}
```

## Migration from EditorView

If migrating from the existing `EditorView` (Java/XML):

**Before (Java/XML)**:
```java
EditorView editorView = findViewById(R.id.editor);
editor.setView(editorView);
```

**After (Compose)**:
```kotlin
@Composable
fun CalculatorScreen(editor: Editor) {
    val state by editor.stateFlow.collectAsState()

    CalculatorEditor(
        state = state,
        onTextChange = { editor.setText(it) },
        onSelectionChange = { editor.setSelection(it) }
    )
}
```

## Theming

The component automatically adapts to your Material3 theme:

```kotlin
// Light theme
CalculatorTheme(theme = CalculatorTheme.MATERIAL_LIGHT) {
    CalculatorEditor(...)
}

// Dark theme
CalculatorTheme(theme = CalculatorTheme.MATERIAL_DARK) {
    CalculatorEditor(...)
}

// Custom theme
MaterialTheme(
    colorScheme = myCustomColorScheme
) {
    CalculatorEditor(...)
}
```

## Related Components

- **CalculatorDisplay**: Shows calculation results with error handling
- **CalculatorKeyboard**: Input keyboard with drag gestures
- **CalculatorButton**: Individual calculator buttons

## License

Copyright 2013 serso aka se.solovyev

Licensed under the Apache License, Version 2.0
