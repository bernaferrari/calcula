package org.solovyev.android.calculator.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import org.solovyev.android.calculator.AppPreferences
import org.solovyev.android.calculator.CalculatorViewModel
import org.solovyev.android.calculator.ui.theme.CalculatorTheme
import org.solovyev.android.calculator.ui.variables.VariablesScreen
import org.solovyev.android.calculator.ui.functions.FunctionsScreen
import org.solovyev.android.calculator.ui.onboarding.OnboardingScreen
import org.solovyev.android.calculator.ui.history.HistoryScreen
import org.solovyev.android.calculator.ui.history.HistoryViewModel
import org.solovyev.android.calculator.ui.about.AboutScreen
import org.solovyev.android.calculator.GuiMode
import org.solovyev.android.calculator.GuiTheme
import org.solovyev.android.calculator.ui.converter.ConverterDialog

@Composable
fun CalculatorApp(
    viewModel: CalculatorViewModel = koinViewModel(),
    historyViewModel: HistoryViewModel = koinViewModel(),
    appPreferences: AppPreferences = koinInject()
) {
    val onboardingFinished by appPreferences.wizard.finished.collectAsState(initial = true)
    
    var currentScreen by rememberSaveable(onboardingFinished) { 
        mutableStateOf<NavigationDestination>(
            if (!onboardingFinished) NavigationDestination.Onboarding 
            else NavigationDestination.Calculator
        ) 
    }
    var showConverter by rememberSaveable { mutableStateOf(false) }

    val themePreference by appPreferences.gui.theme.collectAsState(initial = GuiTheme.material_theme.id)
    val modeState by appPreferences.gui.mode.collectAsState(initial = GuiMode.modern.id)
    val highContrast by appPreferences.gui.highContrast.collectAsState(initial = false)
    val vibrateOnKeypress by appPreferences.gui.vibrateOnKeypress.collectAsState(initial = true)

    // Calculator State
    val displayState by viewModel.displayState.collectAsState()
    val editorState by viewModel.editorState.collectAsState()

    val clipboardManager = LocalClipboardManager.current

    // Map theme string to GuiTheme to determine dark/light mode
    val theme = GuiTheme.fromId(themePreference)
    val isDarkTheme = theme == GuiTheme.material_dark || theme == GuiTheme.material_theme
    
    CalculatorTheme(darkTheme = isDarkTheme) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState == NavigationDestination.Calculator) {
                     slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                } else {
                     slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
                }
            },
            label = "ScreenTransition"
        ) { targetScreen ->
            when (targetScreen) {
                NavigationDestination.Onboarding -> {
                    OnboardingScreen(
                        onComplete = { 
                            currentScreen = NavigationDestination.Calculator
                        },
                        onThemeSelected = { selectedTheme ->
                            viewModel.setTheme(selectedTheme)
                        },
                        onModeSelected = { mode ->
                            viewModel.setMode(mode)
                        }
                    )
                }
                NavigationDestination.Calculator -> {
                    val isModernMode = modeState == GuiMode.modern.id
                    val keyboardMode = when (modeState) {
                        GuiMode.engineer.id -> KeyboardMode.ENGINEER
                        GuiMode.simple.id -> KeyboardMode.SIMPLE
                        GuiMode.modern.id -> KeyboardMode.MODERN
                        else -> KeyboardMode.MODERN
                    }

                    CalculatorScreen(
                        displayState = displayState,
                        editorState = editorState,
                        onEditorTextChange = { text, _ -> viewModel.onEditorTextChange(text) },
                        onEditorSelectionChange = { viewModel.onEditorSelectionChange(it) },
                        highlightExpressions = true,
                        highContrast = highContrast,
                        hapticsEnabled = vibrateOnKeypress,
                        keyboard = { modifier ->
                            CalculatorKeyboard(
                                mode = keyboardMode,
                                actions = rememberKeyboardActions(viewModel),
                                modifier = modifier
                            )
                        },
                        onEquals = { viewModel.onEquals() },
                        onSimplify = { },
                        onPlot = { },
                        overlayContent = { },
                        bottomBar = {
                            if (isModernMode) {
                                ModernModeBottomBar(
                                    viewModel = viewModel,
                                    onOpenSettings = { currentScreen = NavigationDestination.Settings },
                                    onOpenHistory = { currentScreen = NavigationDestination.History },
                                    onOpenConverter = { showConverter = true }
                                )
                            }
                        }
                    )
                }
                NavigationDestination.History -> {
                    val recent by historyViewModel.recent.collectAsState()
                    val saved by historyViewModel.saved.collectAsState()
                    
                    HistoryScreen(
                        recent = recent,
                        saved = saved,
                        onUse = { state -> 
                            viewModel.onEditorTextChange(state.editor.getTextString())
                            currentScreen = NavigationDestination.Calculator
                        },
                        onCopyExpression = { state -> 
                            clipboardManager.setText(AnnotatedString(state.editor.getTextString()))
                        },
                        onCopyResult = { state -> 
                            clipboardManager.setText(AnnotatedString(state.display.text))
                        },
                        onSave = historyViewModel::onSave,
                        onEdit = historyViewModel::onEdit,
                        onDelete = historyViewModel::onDelete,
                        onClearRecent = historyViewModel::onClearRecent,
                        onClearSaved = historyViewModel::onClearSaved,
                        onBack = { currentScreen = NavigationDestination.Calculator }
                    )
                }
                NavigationDestination.Variables -> {
                    VariablesScreen(
                        onBack = { currentScreen = NavigationDestination.Calculator }
                    )
                }
                NavigationDestination.Functions -> {
                    FunctionsScreen(
                        onBack = { currentScreen = NavigationDestination.Calculator }
                    )
                }
                NavigationDestination.About -> {
                    AboutScreen(
                        onBack = { currentScreen = NavigationDestination.Calculator }
                    )
                }
                NavigationDestination.Settings -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Settings (Coming Soon)",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
                NavigationDestination.Converter -> { }
            }
        }

        if (showConverter) {
            ConverterDialog(
                onDismissRequest = { showConverter = false }
            )
        }
    }
}

@Composable
private fun rememberKeyboardActions(viewModel: CalculatorViewModel): KeyboardActions {
    val clipboardManager = LocalClipboardManager.current
    return object : KeyboardActions {
        override fun onNumberClick(number: String) = viewModel.onDigitPressed(number)
        override fun onOperatorClick(operator: String) = viewModel.onOperatorPressed(operator)
        override fun onFunctionClick(function: String) = viewModel.insert(function)
        override fun onSpecialClick(action: String) = viewModel.onSpecialClick(action)
        override fun onClear() = viewModel.onClear()
        override fun onDelete() { viewModel.onBackspace() }
        override fun onEquals() = viewModel.onEquals()
        override fun onMemoryRecall() = viewModel.memoryRecall()
        override fun onMemoryPlus() = viewModel.memoryAdd()
        override fun onMemoryMinus() { }
        override fun onMemoryClear() = viewModel.memoryClear()
        override fun onCursorLeft() = viewModel.moveCursorLeft()
        override fun onCursorRight() = viewModel.moveCursorRight()
        override fun onCursorToStart() { }
        override fun onCursorToEnd() { }
        override fun onCopy() {
            viewModel.getTextToCopy()?.let { text ->
                clipboardManager.setText(AnnotatedString(text))
                viewModel.onCopied()
            }
        }
        override fun onPaste() { }
        override fun onOpenVars() { }
        override fun onOpenFunctions() { }
        override fun onOpenHistory() { }
    }
}
