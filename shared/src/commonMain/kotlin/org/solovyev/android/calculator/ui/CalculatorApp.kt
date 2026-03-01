package org.solovyev.android.calculator.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import org.solovyev.android.calculator.AppPreferences
import org.solovyev.android.calculator.CalculatorViewModel
import org.solovyev.android.calculator.sound.CalculatorSoundManager
import org.solovyev.android.calculator.sound.LocalCalculatorSoundManager
import org.solovyev.android.calculator.sound.LocalCalculatorSoundsEnabled
import org.solovyev.android.calculator.ui.theme.CalculatorTheme
import org.solovyev.android.calculator.ui.variables.VariablesScreen
import org.solovyev.android.calculator.ui.functions.FunctionsScreen
import org.solovyev.android.calculator.ui.onboarding.OnboardingScreen
import org.solovyev.android.calculator.ui.history.HistoryScreen
import org.solovyev.android.calculator.ui.history.HistoryViewModel
import org.solovyev.android.calculator.ui.about.AboutScreen
import org.solovyev.android.calculator.GuiMode
import org.solovyev.android.calculator.GuiTheme
import org.solovyev.android.calculator.ui.nb.CalculatorScreenWithStyle
import org.solovyev.android.calculator.ui.nb.UiStyle
import org.solovyev.android.calculator.ui.settings.SettingsDestination
import org.solovyev.android.calculator.ui.settings.SettingsScreen
import org.solovyev.android.calculator.ui.settings.SettingsViewModel
import org.solovyev.android.calculator.ui.formulas.FormulaScreen
import org.solovyev.android.calculator.formulas.FormulaViewModel
import org.solovyev.android.calculator.ui.converter.ConverterDialog
import org.solovyev.android.calculator.ui.graphing.GraphingScreen

// Navigation keys
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
data object FormulasKey : NavKey

@Serializable
data object AboutKey : NavKey

@Serializable
data object ConverterKey : NavKey

@Serializable
data object GraphKey : NavKey

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(OnboardingKey::class, OnboardingKey.serializer())
            subclass(CalculatorKey::class, CalculatorKey.serializer())
            subclass(HistoryKey::class, HistoryKey.serializer())
            subclass(SettingsKey::class, SettingsKey.serializer())
            subclass(VariablesKey::class, VariablesKey.serializer())
            subclass(FunctionsKey::class, FunctionsKey.serializer())
            subclass(FormulasKey::class, FormulasKey.serializer())
            subclass(AboutKey::class, AboutKey.serializer())
            subclass(ConverterKey::class, ConverterKey.serializer())
            subclass(GraphKey::class, GraphKey.serializer())
        }
    }
}

@Composable
fun CalculatorApp(
    initialExpression: String? = null,
    openHistory: Boolean = false,
    onInitialExpressionConsumed: () -> Unit = {},
    viewModel: CalculatorViewModel = koinViewModel(),
    historyViewModel: HistoryViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    appPreferences: AppPreferences = koinInject()
) {
    val onboardingFinished by appPreferences.wizard.finished.collectAsState(initial = true)

    LaunchedEffect(initialExpression) {
        initialExpression?.let { expression ->
            viewModel.onEditorTextChange(expression, expression.length)
            onInitialExpressionConsumed()
        }
    }

    val backStack = rememberNavBackStack(
        navConfig,
        if (!onboardingFinished) OnboardingKey else CalculatorKey
    )

    LaunchedEffect(openHistory, onboardingFinished) {
        if (onboardingFinished && openHistory) {
            backStack.pushUnique(HistoryKey)
        }
    }

    var settingsDestination by rememberSaveable { mutableStateOf(SettingsDestination.MAIN) }

    val themePreference by appPreferences.gui.theme.collectAsState(initial = GuiTheme.material_theme.id)
    val modeState by appPreferences.gui.mode.collectAsState(initial = GuiMode.modern.id)
    val highContrast by appPreferences.gui.highContrast.collectAsState(initial = false)
    val vibrateOnKeypress by appPreferences.gui.vibrateOnKeypress.collectAsState(initial = true)
    val themeSeed by appPreferences.gui.themeSeed.collectAsState(initial = 0xFF13ABF1.toInt())
    val dynamicColor by appPreferences.gui.dynamicColor.collectAsState(initial = true)
    val soundEnabled by appPreferences.sound.enabled.collectAsState(initial = true)
    val soundIntensity by appPreferences.sound.intensity.collectAsState(initial = 70)
    val gestureAutoActivation by appPreferences.gestures.gestureAutoActivationEnabled.collectAsState(initial = false)
    val showBottomRightEqualsKey by appPreferences.gestures.bottomRightEqualsEnabled.collectAsState(initial = false)

    // Initialize and cleanup sound manager
    val soundManager = koinInject<CalculatorSoundManager>()
    LaunchedEffect(Unit) {
        soundManager.initialize()
    }
    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

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
    val numeralBase by viewModel.numeralBase.collectAsState()

    val clipboardManager = LocalClipboardManager.current

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
                fadeIn(animationSpec = tween(200)) togetherWith
                fadeOut(animationSpec = tween(150))
            },
            popTransitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith
                fadeOut(animationSpec = tween(150))
            },
            onBack = { backStack.popOrCalculator() },
            entryProvider = entryProvider {
                entry<OnboardingKey> {
                    OnboardingScreen(
                        onComplete = {
                            backStack.pushUnique(CalculatorKey)
                            backStack.remove(OnboardingKey)
                        }
                    )
                }
                entry<CalculatorKey> {
                    val keyboardMode = when (GuiMode.fromId(modeState)) {
                        GuiMode.simple -> KeyboardMode.MODERN
                        GuiMode.engineer -> KeyboardMode.ENGINEER
                        GuiMode.modern -> KeyboardMode.MODERN
                    }

                    val recentHistory by viewModel.recentHistory.collectAsState()

                    // Provide sound manager and enabled state via CompositionLocal
                    CompositionLocalProvider(
                        LocalCalculatorSoundManager provides soundManager,
                        LocalCalculatorSoundsEnabled provides soundEnabled
                    ) {
                        CalculatorScreenWithStyle(
                            uiStyle = UiStyle.CLASSIC,
                            keyboardMode = keyboardMode,
                            displayState = displayState,
                            editorState = editorState,
                            previewResult = previewResult,
                            unitHint = unitHint,
                            rpnMode = rpnMode,
                            rpnStack = rpnStack,
                            tapeMode = tapeMode,
                            tapeEntries = tapeEntries,
                            liveTapeEntry = liveTapeEntry,
                            recentHistory = recentHistory,
                            memoryActiveRegister = null,
                            numeralBase = numeralBase,
                            bitwiseWordSize = 64,
                            bitwiseSigned = true,
                            bitwiseOverflow = false,
                            onEditorTextChange = { text, selection -> viewModel.onEditorTextChange(text, selection) },
                            onEditorSelectionChange = { viewModel.onEditorSelectionChange(it) },
                            onOpenHistory = { backStack.pushUnique(HistoryKey) },
                            onOpenSettings = {
                                settingsDestination = SettingsDestination.MAIN
                                backStack.pushUnique(SettingsKey)
                            },
                            onOpenVariables = { backStack.pushUnique(VariablesKey) },
                            onOpenFunctions = { backStack.pushUnique(FunctionsKey) },
                            onOpenConverter = { backStack.pushUnique(ConverterKey) },
                            onOpenGraph = { backStack.pushUnique(GraphKey) },
                            onOpenFormulas = { backStack.pushUnique(FormulasKey) },
                            onOpenAbout = { backStack.pushUnique(AboutKey) },
                            showBottomToolbar = true,
                            onClearTape = viewModel::clearTape,
                            onHistoryItemClick = { state ->
                                viewModel.onEditorTextChange(
                                    text = state.editor.getTextString(),
                                    selection = state.editor.selection
                                )
                            },
                            onHistoryItemDelete = null, // Optional - can be implemented
                            hapticsEnabled = vibrateOnKeypress,
                            soundsEnabled = soundEnabled,
                            gestureAutoActivation = gestureAutoActivation,
                            showBottomRightEqualsKey = showBottomRightEqualsKey,
                            reduceMotion = false,
                            fontScale = 1.0f,
                            keyboardActions = rememberKeyboardActions(
                                viewModel = viewModel,
                                onOpenSettings = {
                                    settingsDestination = SettingsDestination.MAIN
                                    backStack.pushUnique(SettingsKey)
                                },
                                onOpenHistory = { backStack.pushUnique(HistoryKey) },
                                onOpenFunctions = { backStack.pushUnique(FunctionsKey) },
                                onOpenVars = { backStack.pushUnique(VariablesKey) },
                                onOpenGraph = { backStack.pushUnique(GraphKey) }
                            )
                        )
                    }
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
                            backStack.popOrCalculator()
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
                entry<FormulasKey> {
                    val formulaViewModel = koinViewModel<FormulaViewModel>()

                    FormulaScreen(
                        viewModel = formulaViewModel,
                        onUseResult = { result ->
                            viewModel.insert(result)
                            backStack.popOrCalculator()
                        },
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
                entry<AboutKey> {
                    AboutScreen(
                        onBack = { backStack.popOrCalculator() }
                    )
                }
                entry<ConverterKey> {
                    ConverterDialog(
                        onDismissRequest = { backStack.popOrCalculator() }
                    )
                }
                entry<GraphKey> {
                    GraphingScreen(
                        initialExpression = editorState.text.toString(),
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
                                fadeIn(animationSpec = tween(200)) togetherWith
                                fadeOut(animationSpec = tween(150))
                            },
                            label = "SettingsTransition"
                        ) { destination ->
                            SettingsScreen(
                                destination = destination,
                                state = settingsState,
                                actions = settingsViewModel,
                                onNavigate = { nextDestination -> settingsDestination = nextDestination },
                                onStartWizard = { settingsDestination = SettingsDestination.MAIN },
                                onOpenAbout = { backStack.pushUnique(AboutKey) },
                                onReportBug = {},
                                languages = emptyList(),
                                adFreePurchased = true
                            )
                        }
                    }
                }
            }
        )
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
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ),
        topBar = {
            StandardTopAppBar(
                title = title,
                onBack = onBack,
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

@Composable
private fun settingsTitle(destination: SettingsDestination): String = when (destination) {
    SettingsDestination.MAIN -> "Settings"
    SettingsDestination.NUMBER_FORMAT -> "Number Format"
    SettingsDestination.APPEARANCE -> "Appearance"
    SettingsDestination.ACCESSIBILITY -> "Accessibility"
    SettingsDestination.WIDGET -> "Widget"
    SettingsDestination.OTHER -> "Advanced"
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

private fun MutableList<NavKey>.pushUnique(key: NavKey) {
    if (lastOrNull() == key) return
    add(key)
}

@Composable
private fun rememberKeyboardActions(
    viewModel: CalculatorViewModel,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFunctions: () -> Unit,
    onOpenVars: () -> Unit,
    onOpenGraph: () -> Unit
): KeyboardActions {
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
        override fun onSetBitwiseOverflow(overflow: Boolean) {}
        override fun onCursorLeft() = viewModel.moveCursorLeft()
        override fun onCursorRight() = viewModel.moveCursorRight()
        override fun onCursorToStart() = viewModel.moveCursorToStart()
        override fun onCursorToEnd() = viewModel.moveCursorToEnd()
        override fun onCopy() {
            // Handled at screen level
        }
        override fun onPaste() {
            // Handled at screen level
        }
        override fun onOpenVars() = onOpenVars()
        override fun onOpenFunctions() = onOpenFunctions()
        override fun onOpenHistory() = onOpenHistory()
        override fun onOpenGraph() = onOpenGraph()
        override fun onSwipeUp(buttonId: String) {}
        override fun onSwipeDown(buttonId: String) {}
        override fun onSwipeLeft(buttonId: String) {}
        override fun onSwipeRight(buttonId: String) {}
        override fun onLongPress(buttonId: String) {}
        override fun onDoubleTap(buttonId: String) {}
    }
}
