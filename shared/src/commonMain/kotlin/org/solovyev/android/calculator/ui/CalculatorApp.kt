package org.solovyev.android.calculator.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
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
import org.solovyev.android.calculator.ui.nb.CalculatorScreenWithStyle
import org.solovyev.android.calculator.ui.nb.UiStyle
import org.solovyev.android.calculator.ui.settings.SettingsDestination
import org.solovyev.android.calculator.ui.settings.SettingsScreen
import org.solovyev.android.calculator.ui.settings.SettingsViewModel
import org.solovyev.android.calculator.ui.graphing.GraphingScreen

// Navigation keys for Navigation 3
@Serializable
data object OnboardingKey : NavKey

@Serializable
data object CalculatorKey : NavKey

@Serializable
data object HistoryKey : NavKey

@Serializable
data object SettingsKey : NavKey

@Serializable
data object VariablesKey : NavKey

@Serializable
data object FunctionsKey : NavKey

@Serializable
data object GraphKey : NavKey

@Serializable
data object AboutKey : NavKey

// SavedState configuration for multiplatform navigation
private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(OnboardingKey::class, OnboardingKey.serializer())
            subclass(CalculatorKey::class, CalculatorKey.serializer())
            subclass(HistoryKey::class, HistoryKey.serializer())
            subclass(SettingsKey::class, SettingsKey.serializer())
            subclass(VariablesKey::class, VariablesKey.serializer())
            subclass(FunctionsKey::class, FunctionsKey.serializer())
            subclass(GraphKey::class, GraphKey.serializer())
            subclass(AboutKey::class, AboutKey.serializer())
        }
    }
}

@Composable
fun CalculatorApp(
    viewModel: CalculatorViewModel = koinViewModel(),
    historyViewModel: HistoryViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    appPreferences: AppPreferences = koinInject()
) {
    val onboardingFinished by appPreferences.wizard.finished.collectAsState(initial = true)
    
    // Navigation 3 back stack with SavedStateConfiguration for multiplatform support
    val backStack = rememberNavBackStack(
        navConfig,
        if (!onboardingFinished) OnboardingKey else CalculatorKey
    )

    var showConverter by rememberSaveable { mutableStateOf(false) }
    var settingsDestination by rememberSaveable { mutableStateOf(SettingsDestination.MAIN) }

    val themePreference by appPreferences.gui.theme.collectAsState(initial = GuiTheme.material_theme.id)
    val modeState by appPreferences.gui.mode.collectAsState(initial = GuiMode.modern.id)
    val highContrast by appPreferences.gui.highContrast.collectAsState(initial = false)
    val vibrateOnKeypress by appPreferences.gui.vibrateOnKeypress.collectAsState(initial = true)
    val themeSeed by appPreferences.gui.themeSeed.collectAsState(initial = 0xFF13ABF1.toInt())
    val dynamicColor by appPreferences.gui.dynamicColor.collectAsState(initial = true)

    // Calculator State
    val displayState by viewModel.displayState.collectAsState()
    val editorState by viewModel.editorState.collectAsState()
    val previewResult by viewModel.previewResult.collectAsState()
    val unitHint by viewModel.unitHint.collectAsState()
    val calculationLatencyMs by viewModel.calculationLatencyMs.collectAsState()
    val rpnMode by viewModel.rpnMode.collectAsState()
    val rpnStack by viewModel.rpnStack.collectAsState()
    val tapeMode by viewModel.tapeMode.collectAsState()
    val tapeEntries by viewModel.tapeEntries.collectAsState()
    val liveTapeEntry by viewModel.liveTapeEntry.collectAsState()
    val memoryActiveRegister by viewModel.memoryActiveRegister.collectAsState()
    val numeralBase by viewModel.numeralBase.collectAsState()
    val bitwiseWordSize by viewModel.bitwiseWordSize.collectAsState()
    val bitwiseSigned by viewModel.bitwiseSigned.collectAsState()
    val bitwiseOverflow by viewModel.bitwiseOverflow.collectAsState()

    val clipboardManager = LocalClipboardManager.current

    // Map theme string to GuiTheme to determine dark/light mode
    val theme = GuiTheme.fromId(themePreference)
    val isDarkTheme = when (theme) {
        GuiTheme.material_dark -> true
        GuiTheme.material_light -> false
        GuiTheme.material_theme -> isSystemInDarkTheme()
    }

    CalculatorTheme(
        darkTheme = isDarkTheme,
        seedColor = Color(themeSeed),
        useDynamicColor = dynamicColor
    ) {
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                (
                    slideInHorizontally(
                        initialOffsetX = { width -> width / 3 },
                        animationSpec = tween(durationMillis = 280)
                    ) + fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 40))
                ) togetherWith (
                    slideOutHorizontally(
                        targetOffsetX = { width -> -width / 4 },
                        animationSpec = tween(durationMillis = 240)
                    ) + fadeOut(animationSpec = tween(durationMillis = 180))
                )
            },
            popTransitionSpec = {
                (
                    slideInHorizontally(
                        initialOffsetX = { width -> -width / 3 },
                        animationSpec = tween(durationMillis = 280)
                    ) + fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 40))
                ) togetherWith (
                    slideOutHorizontally(
                        targetOffsetX = { width -> width / 4 },
                        animationSpec = tween(durationMillis = 240)
                    ) + fadeOut(animationSpec = tween(durationMillis = 180))
                )
            },
            onBack = { backStack.popOrCalculator() },
            entryProvider = entryProvider {
                entry<OnboardingKey> {
                    OnboardingScreen(
                        onComplete = { 
                            backStack.add(CalculatorKey)
                            backStack.remove(OnboardingKey)
                        },
                        onThemeSelected = { selectedTheme ->
                            viewModel.setTheme(selectedTheme)
                        },
                        onModeSelected = { mode ->
                            viewModel.setMode(mode)
                        }
                    )
                }
                entry<CalculatorKey> {
                    val keyboardMode = when (GuiMode.fromId(modeState)) {
                        GuiMode.simple -> KeyboardMode.MINIMAL
                        GuiMode.engineer -> KeyboardMode.ENGINEER
                        GuiMode.modern -> KeyboardMode.MODERN
                    }

                    // Classic style keeps a dedicated result panel visible.
                    CalculatorScreenWithStyle(
                        uiStyle = UiStyle.CLASSIC,
                        keyboardMode = keyboardMode,
                        displayState = displayState,
                        editorState = editorState,
                        previewResult = previewResult,
                        unitHint = unitHint,
                        calculationLatencyMs = calculationLatencyMs,
                        rpnMode = rpnMode,
                        rpnStack = rpnStack,
                        tapeMode = tapeMode,
                        tapeEntries = tapeEntries,
                        liveTapeEntry = liveTapeEntry,
                        memoryActiveRegister = memoryActiveRegister,
                        numeralBase = numeralBase,
                        bitwiseWordSize = bitwiseWordSize,
                        bitwiseSigned = bitwiseSigned,
                        bitwiseOverflow = bitwiseOverflow,
                        onEditorTextChange = { text, selection -> viewModel.onEditorTextChange(text, selection) },
                        onEditorSelectionChange = { viewModel.onEditorSelectionChange(it) },
                        onOpenHistory = { backStack.add(HistoryKey) },
                        onOpenConverter = { showConverter = true },
                        onOpenFunctions = { backStack.add(FunctionsKey) },
                        onOpenVars = { backStack.add(VariablesKey) },
                        onOpenGraph = { backStack.add(GraphKey) },
                        onOpenSettings = {
                            settingsDestination = SettingsDestination.MAIN
                            backStack.add(SettingsKey)
                        },
                        highlightExpressions = true,
                        highContrast = highContrast,
                        hapticsEnabled = vibrateOnKeypress,
                        showBottomToolbar = true,
                        onClearTape = viewModel::clearTape,
                        keyboardActions = rememberKeyboardActions(
                            viewModel = viewModel,
                            onOpenSettings = {
                                settingsDestination = SettingsDestination.MAIN
                                backStack.add(SettingsKey)
                            },
                            onOpenHistory = { backStack.add(HistoryKey) },
                            onOpenFunctions = { backStack.add(FunctionsKey) },
                            onOpenVars = { backStack.add(VariablesKey) },
                        ),
                    )
                }
                entry<HistoryKey> {
                    val recent by historyViewModel.recent.collectAsState()
                    val saved by historyViewModel.saved.collectAsState()
                    
                    HistoryScreen(
                        recent = recent,
                        saved = saved,
                        onUse = { state -> 
                            viewModel.onEditorTextChange(
                                text = state.editor.getTextString(),
                                selection = state.editor.selection
                            )
                            backStack.removeLastOrNull()
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
                        onBack = { backStack.popOrCalculator() }
                    )
                }
                entry<VariablesKey> {
                    VariablesScreen(
                        onBack = { backStack.popOrCalculator() }
                    )
                }
                entry<FunctionsKey> {
                    FunctionsScreen(
                        onBack = { backStack.popOrCalculator() }
                    )
                }
                entry<GraphKey> {
                    GraphingScreen(
                        onBack = { backStack.popOrCalculator() }
                    )
                }
                entry<AboutKey> {
                    AboutScreen(
                        onBack = { backStack.popOrCalculator() }
                    )
                }
                entry<SettingsKey> {
                    val settingsState by settingsViewModel.state.collectAsState()

                    BackHandler(enabled = settingsDestination != SettingsDestination.MAIN) {
                        settingsDestination = SettingsDestination.MAIN
                    }

                    SettingsScaffold(
                        title = settingsTitle(settingsDestination),
                        onBack = {
                            if (settingsDestination == SettingsDestination.MAIN) {
                                backStack.popOrCalculator()
                            } else {
                                settingsDestination = SettingsDestination.MAIN
                            }
                        }
                    ) {
                        AnimatedContent(
                            targetState = settingsDestination,
                            transitionSpec = {
                                if (settingsDestinationLevel(targetState) >= settingsDestinationLevel(initialState)) {
                                    (
                                        slideInHorizontally(
                                            initialOffsetX = { width -> width / 3 },
                                            animationSpec = tween(durationMillis = 260)
                                        ) + fadeIn(animationSpec = tween(durationMillis = 200, delayMillis = 30))
                                    ) togetherWith (
                                        slideOutHorizontally(
                                            targetOffsetX = { width -> -width / 4 },
                                            animationSpec = tween(durationMillis = 220)
                                        ) + fadeOut(animationSpec = tween(durationMillis = 160))
                                    )
                                } else {
                                    (
                                        slideInHorizontally(
                                            initialOffsetX = { width -> -width / 3 },
                                            animationSpec = tween(durationMillis = 260)
                                        ) + fadeIn(animationSpec = tween(durationMillis = 200, delayMillis = 30))
                                    ) togetherWith (
                                        slideOutHorizontally(
                                            targetOffsetX = { width -> width / 4 },
                                            animationSpec = tween(durationMillis = 220)
                                        ) + fadeOut(animationSpec = tween(durationMillis = 160))
                                    )
                                }
                            },
                            label = "SettingsDestinationTransition"
                        ) { destination ->
                            SettingsScreen(
                                destination = destination,
                                state = settingsState,
                                actions = settingsViewModel,
                                onNavigate = { nextDestination -> settingsDestination = nextDestination },
                                onStartWizard = {
                                    settingsDestination = SettingsDestination.MAIN
                                    backStack.add(OnboardingKey)
                                },
                                onOpenAbout = { backStack.add(AboutKey) },
                                onReportBug = {},
                                onSupportProject = {},
                                languages = emptyList(),
                                adFreePurchased = true,
                            )
                        }
                    }
                }
            }
        )

        if (showConverter) {
            ConverterDialog(
                onDismissRequest = { showConverter = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ),
        topBar = {
            StandardTopAppBar(
                title = title,
                onBack = onBack,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

@Composable
private fun settingsTitle(destination: SettingsDestination): String = when (destination) {
    SettingsDestination.MAIN -> stringResource(Res.string.cpp_settings)
    SettingsDestination.NUMBER_FORMAT -> stringResource(Res.string.cpp_number_format)
    SettingsDestination.APPEARANCE -> stringResource(Res.string.cpp_appearance)
    SettingsDestination.WIDGET -> stringResource(Res.string.cpp_widget)
    SettingsDestination.OTHER -> stringResource(Res.string.cpp_other)
}

private fun settingsDestinationLevel(destination: SettingsDestination): Int = when (destination) {
    SettingsDestination.MAIN -> 0
    SettingsDestination.NUMBER_FORMAT,
    SettingsDestination.APPEARANCE,
    SettingsDestination.WIDGET,
    SettingsDestination.OTHER -> 1
}

private fun MutableList<NavKey>.popOrCalculator() {
    if (size > 1) {
        removeLastOrNull()
        return
    }
    if (firstOrNull() != CalculatorKey) {
        clear()
        add(CalculatorKey)
    }
}

@Composable
private fun rememberKeyboardActions(
    viewModel: CalculatorViewModel,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFunctions: () -> Unit,
    onOpenVars: () -> Unit
): KeyboardActions {
    val clipboardManager = LocalClipboardManager.current
    return object : KeyboardActions {
        override fun onNumberClick(number: String) = viewModel.onDigitPressed(number)
        override fun onOperatorClick(operator: String) = viewModel.onOperatorPressed(operator)
        override fun onFunctionClick(function: String) = viewModel.onFunctionPressed(function)
        override fun onSpecialClick(action: String) = viewModel.onSpecialClick(action)
        override fun onSimplify() = viewModel.onSimplify()
        override fun onOpenSettings() = onOpenSettings()
        override fun onClear() = viewModel.onClear()
        override fun onDelete() { viewModel.onBackspace() }
        override fun onEquals() = viewModel.onEquals()
        override fun onMemoryStore() = viewModel.memoryStore()
        override fun onMemoryRecall() = viewModel.memoryRecall()
        override fun onMemoryPlus() = viewModel.memoryAdd()
        override fun onMemoryMinus() = viewModel.memorySubtract()
        override fun onMemoryClear() = viewModel.memoryClear()
        override fun onMemoryRegisterSelected(register: String) = viewModel.selectMemoryRegister(register)
        override fun onSetNumeralBase(base: jscl.NumeralBase) = viewModel.setNumeralBase(base)
        override fun onSetBitwiseWordSize(size: Int) = viewModel.setBitwiseWordSize(size)
        override fun onSetBitwiseSigned(signed: Boolean) = viewModel.setBitwiseSigned(signed)
        override fun onCursorLeft() = viewModel.moveCursorLeft()
        override fun onCursorRight() = viewModel.moveCursorRight()
        override fun onCursorToStart() = viewModel.moveCursorToStart()
        override fun onCursorToEnd() = viewModel.moveCursorToEnd()
        override fun onCopy() {
            viewModel.getTextToCopy()?.let { text ->
                clipboardManager.setText(AnnotatedString(text))
                viewModel.onCopied()
            }
        }
        override fun onPaste() {
            clipboardManager.getText()?.text?.let { text ->
                if (text.isNotBlank()) {
                    viewModel.onPasteText(text)
                }
            }
        }
        override fun onOpenVars() = onOpenVars()
        override fun onOpenFunctions() = onOpenFunctions()
        override fun onOpenHistory() = onOpenHistory()
    }
}
