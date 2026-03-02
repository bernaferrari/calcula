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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.stringResource
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
import org.solovyev.android.calculator.ui.about.AboutActions
import org.solovyev.android.calculator.GuiMode
import org.solovyev.android.calculator.GuiTheme
import org.solovyev.android.calculator.ui.settings.Language
import org.solovyev.android.calculator.ui.settings.SettingsDestination
import org.solovyev.android.calculator.ui.settings.SettingsScreen
import org.solovyev.android.calculator.ui.settings.SettingsViewModel
import org.solovyev.android.calculator.ui.formulas.FormulaScreen
import org.solovyev.android.calculator.formulas.FormulaViewModel
import org.solovyev.android.calculator.ui.converter.ConverterDialog
import org.solovyev.android.calculator.ui.graphing.GraphingScreen
import org.solovyev.android.calculator.preferences.DataStoreGuiPreferences

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
    openConverter: Boolean = false,
    openHistory: Boolean = false,
    openSettings: Boolean = false,
    openVariables: Boolean = false,
    openFunctions: Boolean = false,
    onInitialExpressionConsumed: () -> Unit = {},
    onOpenConverterConsumed: () -> Unit = {},
    onOpenHistoryConsumed: () -> Unit = {},
    onOpenSettingsConsumed: () -> Unit = {},
    onOpenVariablesConsumed: () -> Unit = {},
    onOpenFunctionsConsumed: () -> Unit = {},
    availableLanguages: List<Language> = emptyList(),
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
    var settingsDestination by rememberSaveable { mutableStateOf(SettingsDestination.MAIN) }
    var isConverterSheetVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(openConverter, onboardingFinished) {
        if (onboardingFinished && openConverter) {
            if (backStack.lastOrNull() != CalculatorKey) {
                backStack.pushUnique(CalculatorKey)
            }
            isConverterSheetVisible = true
            onOpenConverterConsumed()
        }
    }

    LaunchedEffect(openHistory, onboardingFinished) {
        if (onboardingFinished && openHistory) {
            backStack.pushUnique(HistoryKey)
            onOpenHistoryConsumed()
        }
    }

    LaunchedEffect(openSettings, onboardingFinished) {
        if (onboardingFinished && openSettings) {
            settingsDestination = SettingsDestination.MAIN
            backStack.pushUnique(SettingsKey)
            onOpenSettingsConsumed()
        }
    }

    LaunchedEffect(openVariables, onboardingFinished) {
        if (onboardingFinished && openVariables) {
            backStack.pushUnique(VariablesKey)
            onOpenVariablesConsumed()
        }
    }

    LaunchedEffect(openFunctions, onboardingFinished) {
        if (onboardingFinished && openFunctions) {
            backStack.pushUnique(FunctionsKey)
            onOpenFunctionsConsumed()
        }
    }
    val scope = rememberCoroutineScope()

    val themePreference by appPreferences.gui.theme.collectAsState(initial = GuiTheme.material_theme.id)
    val modeState by appPreferences.gui.mode.collectAsState(initial = GuiMode.modern.id)
    val highContrast by appPreferences.gui.highContrast.collectAsState(initial = false)
    val reduceMotion by appPreferences.gui.reduceMotion.collectAsState(initial = false)
    val fontScale by appPreferences.gui.fontScale.collectAsState(initial = 1.0f)
    val vibrateOnKeypress by appPreferences.gui.vibrateOnKeypress.collectAsState(initial = true)
    val themeSeed by appPreferences.gui.themeSeed.collectAsState(initial = 0xFF13ABF1.toInt())
    val dynamicColor by appPreferences.gui.dynamicColor.collectAsState(initial = true)
    val soundEnabled by appPreferences.sound.enabled.collectAsState(initial = true)
    val soundIntensity by appPreferences.sound.intensity.collectAsState(initial = 70)
    val gestureAutoActivation by appPreferences.gestures.gestureAutoActivationEnabled.collectAsState(initial = false)
    val showBottomRightEqualsKey by appPreferences.gestures.bottomRightEqualsEnabled.collectAsState(initial = false)
    val layerUpEnabled by appPreferences.gestures.layerUpEnabled.collectAsState(initial = true)
    val layerDownEnabled by appPreferences.gestures.layerDownEnabled.collectAsState(initial = true)
    val layerEngineerEnabled by appPreferences.gestures.layerEngineerEnabled.collectAsState(initial = true)
    val bitwiseWordSize by appPreferences.settings.bitwiseWordSize.collectAsState(initial = 64)
    val bitwiseSigned by appPreferences.settings.bitwiseSigned.collectAsState(initial = true)
    val aboutActions = koinInject<AboutActions>()

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
        CompositionLocalProvider(
            LocalCalculatorHighContrast provides highContrast,
            LocalCalculatorReduceMotion provides reduceMotion,
            LocalCalculatorFontScale provides fontScale
        ) {
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    val settingsContentKey = SettingsKey.toString()
                    val targetIsSettings = targetState.entries.any { it.contentKey == settingsContentKey }
                    val initialIsSettings = initialState.entries.any { it.contentKey == settingsContentKey }
                    if (targetIsSettings && !initialIsSettings) {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(280)
                        ) + fadeIn(animationSpec = tween(180)) togetherWith
                            fadeOut(animationSpec = tween(140))
                    } else {
                        fadeIn(animationSpec = tween(200)) togetherWith
                            fadeOut(animationSpec = tween(150))
                    }
                },
                popTransitionSpec = {
                    val settingsContentKey = SettingsKey.toString()
                    val targetIsSettings = targetState.entries.any { it.contentKey == settingsContentKey }
                    val initialIsSettings = initialState.entries.any { it.contentKey == settingsContentKey }
                    if (initialIsSettings && !targetIsSettings) {
                        fadeIn(animationSpec = tween(180)) togetherWith
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(260)
                            ) + fadeOut(animationSpec = tween(120))
                    } else {
                        fadeIn(animationSpec = tween(200)) togetherWith
                            fadeOut(animationSpec = tween(150))
                    }
                },
                onBack = { backStack.popOrFallback(CalculatorKey) },
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
                    val guiMode = GuiMode.fromId(modeState)
                    val dataStoreGuiPreferences = appPreferences.gui as? DataStoreGuiPreferences
                    val persistedTabsState by dataStoreGuiPreferences
                        ?.calculatorTabsState
                        ?.collectAsState(initial = null)
                        ?: remember { mutableStateOf<String?>(null) }
                    val keyboardMode = when (guiMode) {
                        GuiMode.simple -> KeyboardMode.MODERN
                        GuiMode.engineer -> if (layerEngineerEnabled) KeyboardMode.ENGINEER else KeyboardMode.MODERN
                        GuiMode.modern -> KeyboardMode.MODERN
                    }

                    // Provide sound manager and enabled state via CompositionLocal
                    CompositionLocalProvider(
                        LocalCalculatorSoundManager provides soundManager,
                        LocalCalculatorSoundsEnabled provides soundEnabled
                    ) {
                        val keyboardActions = rememberKeyboardActions(
                            viewModel = viewModel,
                            onOpenSettings = {
                                settingsDestination = SettingsDestination.MAIN
                                backStack.pushUnique(SettingsKey)
                            },
                            onOpenHistory = { backStack.pushUnique(HistoryKey) },
                            onOpenFunctions = { backStack.pushUnique(FunctionsKey) },
                            onOpenVars = { backStack.pushUnique(VariablesKey) },
                            onOpenGraph = { backStack.pushUnique(GraphKey) },
                            onCopy = {
                                if (displayState.valid && displayState.text.isNotBlank()) {
                                    clipboardManager.setText(AnnotatedString(displayState.text))
                                }
                            },
                            onPaste = {
                                val pasted = clipboardManager.getText()?.text.orEmpty()
                                if (pasted.isNotBlank()) {
                                    viewModel.insert(pasted)
                                }
                            }
                        )

                        CalculatorScreen(
                            displayState = displayState,
                            editorState = editorState,
                            previewResult = previewResult,
                            unitHint = unitHint,
                            rpnMode = rpnMode,
                            rpnStack = rpnStack,
                            tapeMode = tapeMode,
                            tapeEntries = tapeEntries,
                            liveTapeEntry = liveTapeEntry,
                            persistedTabsState = persistedTabsState,
                            onPersistTabsState = { serializedState ->
                                dataStoreGuiPreferences?.let { preferences ->
                                    scope.launch {
                                        preferences.setCalculatorTabsState(serializedState)
                                    }
                                }
                            },
                            onCopy = keyboardActions::onCopy,
                            onEquals = keyboardActions::onEquals,
                            onEditorTextChange = { text, selection -> viewModel.onEditorTextChange(text, selection) },
                            onEditorSelectionChange = { viewModel.onEditorSelectionChange(it) },
                            onOpenHistory = { backStack.pushUnique(HistoryKey) },
                            onOpenSettings = {
                                settingsDestination = SettingsDestination.MAIN
                                backStack.pushUnique(SettingsKey)
                            },
                            onOpenVariables = { backStack.pushUnique(VariablesKey) },
                            onOpenFunctions = { backStack.pushUnique(FunctionsKey) },
                            onOpenConverter = { isConverterSheetVisible = true },
                            onOpenGraph = { backStack.pushUnique(GraphKey) },
                            onOpenFormulas = { backStack.pushUnique(FormulasKey) },
                            onOpenAbout = { backStack.pushUnique(AboutKey) },
                            layerUpEnabled = layerUpEnabled,
                            layerDownEnabled = layerDownEnabled,
                            layerEngineerEnabled = layerEngineerEnabled,
                            onSetLayerUpEnabled = { enabled ->
                                scope.launch { appPreferences.gestures.setLayerUpEnabled(enabled) }
                            },
                            onSetLayerDownEnabled = { enabled ->
                                scope.launch { appPreferences.gestures.setLayerDownEnabled(enabled) }
                            },
                            onSetLayerEngineerEnabled = { enabled ->
                                scope.launch { appPreferences.gestures.setLayerEngineerEnabled(enabled) }
                            },
                            onCursorLeft = keyboardActions::onCursorLeft,
                            onCursorRight = keyboardActions::onCursorRight,
                            onCursorToStart = keyboardActions::onCursorToStart,
                            onCursorToEnd = keyboardActions::onCursorToEnd,
                            onDelete = keyboardActions::onDelete,
                            onClearTape = viewModel::clearTape,
                            hapticsEnabled = vibrateOnKeypress,
                            keyboard = { keyboardModifier ->
                                CalculatorKeyboard(
                                    mode = keyboardMode,
                                    actions = keyboardActions,
                                    numeralBase = numeralBase,
                                    bitwiseWordSize = bitwiseWordSize,
                                    bitwiseSigned = bitwiseSigned,
                                    isSimpleMode = guiMode == GuiMode.simple,
                                    gestureAutoActivation = gestureAutoActivation,
                                    showBottomRightEqualsKey = showBottomRightEqualsKey,
                                    layerUpEnabled = layerUpEnabled,
                                    layerDownEnabled = layerDownEnabled,
                                    modifier = keyboardModifier
                                )
                            }
                        )

                        if (isConverterSheetVisible) {
                            ConverterDialog(
                                onDismissRequest = { isConverterSheetVisible = false }
                            )
                        }
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
                            backStack.popOrFallback(CalculatorKey)
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
                        onBack = { backStack.popOrFallback(CalculatorKey) }
                    )
                }
                entry<FormulasKey> {
                    val formulaViewModel = koinViewModel<FormulaViewModel>()

                    FormulaScreen(
                        viewModel = formulaViewModel,
                        onUseResult = { result ->
                            viewModel.insert(result)
                            backStack.popOrFallback(CalculatorKey)
                        },
                        onBack = { backStack.popOrFallback(CalculatorKey) }
                    )
                }
                entry<VariablesKey> {
                    VariablesScreen(
                        onBack = { backStack.popOrFallback(CalculatorKey) }
                    )
                }
                entry<FunctionsKey> {
                    FunctionsScreen(
                        onBack = { backStack.popOrFallback(CalculatorKey) }
                    )
                }
                entry<AboutKey> {
                    AboutScreen(
                        onBack = { backStack.popOrFallback(CalculatorKey) }
                    )
                }
                entry<ConverterKey> {
                    LaunchedEffect(Unit) {
                        isConverterSheetVisible = true
                        backStack.popOrFallback(CalculatorKey)
                    }
                }
                entry<GraphKey> {
                    GraphingScreen(
                        initialExpression = editorState.text.toString(),
                        onBack = { backStack.popOrFallback(CalculatorKey) }
                    )
                }
                entry<SettingsKey> {
                    val settingsState by settingsViewModel.state.collectAsState()
                    val focusManager = LocalFocusManager.current

                    BackHandler(enabled = true) {
                        focusManager.clearFocus(force = true)
                        if (settingsDestination == SettingsDestination.MAIN) {
                            backStack.popOrFallback(CalculatorKey)
                        } else {
                            settingsDestination = SettingsDestination.MAIN
                        }
                    }

                    SettingsScaffold(
                        title = settingsTitle(settingsDestination),
                        onBack = {
                            if (settingsDestination == SettingsDestination.MAIN) {
                                backStack.popOrFallback(CalculatorKey)
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
                                onStartWizard = {
                                    settingsDestination = SettingsDestination.MAIN
                                    scope.launch {
                                        appPreferences.wizard.setFinished(false)
                                    }
                                    backStack.clear()
                                    backStack.add(OnboardingKey)
                                },
                                onOpenAbout = { backStack.pushUnique(AboutKey) },
                                onReportBug = { aboutActions.sendEmail() },
                                languages = availableLanguages,
                                adFreePurchased = true
                            )
                        }
                    }
                }
                }
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
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ),
        topBar = {
            StandardTopAppBar(
                title = title,
                onBack = {
                    focusManager.clearFocus(force = true)
                    onBack()
                },
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
    SettingsDestination.MAIN -> stringResource(Res.string.cpp_settings)
    SettingsDestination.NUMBER_FORMAT -> stringResource(Res.string.cpp_number_format)
    SettingsDestination.APPEARANCE -> stringResource(Res.string.cpp_appearance)
    SettingsDestination.ACCESSIBILITY -> stringResource(Res.string.cpp_accessibility)
    SettingsDestination.WIDGET -> stringResource(Res.string.cpp_widget)
    SettingsDestination.OTHER -> stringResource(Res.string.cpp_prefs_advanced)
}

@Composable
private fun rememberKeyboardActions(
    viewModel: CalculatorViewModel,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFunctions: () -> Unit,
    onOpenVars: () -> Unit,
    onOpenGraph: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit
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
        override fun onCopy() = onCopy()
        override fun onPaste() = onPaste()
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
