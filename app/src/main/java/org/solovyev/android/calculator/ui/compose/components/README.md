# Calculator Compose Components

This package contains Jetpack Compose components for the Android Calculator++ application.

## Components

### 1. CalculatorDisplay.kt
Shows the calculation result with the following features:
- **Auto-resizing text**: Dynamically adjusts font size based on content length
- **Error state**: Displays errors in red color when `DisplayState.valid` is false
- **Static layout**: Legacy-style display without extra chrome

**Usage:**
```kotlin
CalculatorDisplay(
    state = displayState,
    minTextSize = 20.sp,
    maxTextSize = 48.sp
)
```

### 2. CalculatorEditor.kt
Shows the input expression with syntax highlighting:
- **Syntax highlighting**:
  - Numbers: Secondary color
  - Operators (+, -, ×, ÷, etc.): Tertiary color, bold
  - Functions (sin, cos, log, etc.): Primary color, italic
  - Constants (e, π, i): Secondary color, bold
  - Parentheses/brackets: OnSurfaceVariant, bold
- **Blinking cursor**: Animated cursor when editor is empty
- **Text selection**: Full selection support with custom colors
- **Horizontal scrolling**: Auto-scrolls to cursor position

**Usage:**
```kotlin
CalculatorEditor(
    state = editorState,
    onTextChange = { newText, selection ->
        // Handle text and cursor changes together
        editor.setText(newText, selection)
    },
    onSelectionChange = { position ->
        // Handle cursor movement
        editor.setSelection(position)
    }
)
```

### 3. CalculatorScreen.kt
The main calculator screen that combines all components:
- Editor at top (input)
- Display row with equals button
- Keyboard at bottom (buttons)
- Material 3 theming with legacy layout proportions

**Usage:**
```kotlin
CalculatorScreen(
    displayState = displayState,
    editorState = editorState,
    onEditorTextChange = { newText, selection ->
        editor.setText(newText, selection)
    },
    onEditorSelectionChange = { position ->
        editor.setSelection(position)
    },
    keyboard = { modifier ->
        CalculatorKeyboard(
            actions = keyboardActions,
            modifier = modifier
        )
    },
    onEquals = { keyboardActions.onEquals() },
    onSimplify = { keyboardActions.onSpecialClick("≡") },
    onPlot = { keyboardActions.onSpecialClick("\ue009") },
    topBar = {}
)
```

## Integration with Existing Code

These components are designed to work with the existing data models:
- `DisplayState`: From `/app/src/main/java/org/solovyev/android/calculator/DisplayState.kt`
- `EditorState`: From `/app/src/main/java/org/solovyev/android/calculator/EditorState.kt`

### Example Activity Integration

```kotlin
class CalculatorActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CalculatorTheme {
                val displayState by viewModel.displayState.collectAsState()
                val editorState by viewModel.editorState.collectAsState()

                CalculatorScreen(
                    displayState = displayState,
                    editorState = editorState,
                    onEditorTextChange = { text, selection ->
                        viewModel.onTextChanged(text)
                        viewModel.onSelectionChanged(selection)
                    },
                    onEditorSelectionChange = { viewModel.onSelectionChanged(it) },
                    keyboard = { modifier ->
                        CalculatorKeyboard(
                            actions = keyboardActions,
                            modifier = modifier
                        )
                    },
                    onEquals = { keyboardActions.onEquals() },
                    onSimplify = { keyboardActions.onSpecialClick("≡") },
                    onPlot = { keyboardActions.onSpecialClick("\ue009") },
                    topBar = {}
                )
            }
        }
    }
}
```

## Theming

All components use Material 3 theming from `CalculatorTheme` defined in:
- `/app/src/main/java/org/solovyev/android/calculator/ui/compose/theme/Theme.kt`
- `/app/src/main/java/org/solovyev/android/calculator/ui/compose/theme/Color.kt`

Colors used:
- **Display text**: `onSurface` (normal) or `error` (when invalid)
- **Editor text**: `onBackground`
- **Syntax highlighting**:
  - Numbers: `secondary`
  - Operators: `tertiary`
  - Functions: `primary`
  - Constants: `secondary`
- **Backgrounds**:
  - Display: `surface`
  - Editor: `background`

## Animations

### Display Animations
- None (legacy-style static display)

### Editor Animations
- **Cursor blink**: 500ms infinite fade animation
- **Auto-scroll**: Smooth scroll to cursor position

## Dependencies

These components require:
- Jetpack Compose (Material 3)
- Existing calculator models (`DisplayState`, `EditorState`)

## Future Improvements

1. **Full Compose Keyboard**: Replace AndroidView keyboard wrapper with native Compose buttons
2. **Advanced Highlighting**: Integrate with `TextHighlighter.java` for more complex syntax highlighting
3. **Multi-line Editor**: Support for longer expressions with line wrapping
4. **Accessibility**: Add content descriptions and screen reader support
5. **Haptic Feedback**: Add vibration on button presses
6. **Landscape Mode**: Optimize layout for landscape orientation
