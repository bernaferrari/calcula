package org.solovyev.android.calculator

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import jscl.AngleUnit
import jscl.NumeralBase
import org.solovyev.android.calculator.navigation.About
import org.solovyev.android.calculator.navigation.Calculator
import org.solovyev.android.calculator.navigation.HistoryKey
import org.solovyev.android.calculator.navigation.NavKey
import org.solovyev.android.calculator.navigation.Settings
import org.solovyev.android.calculator.navigation.Wizard
import org.solovyev.android.calculator.wizard.WizardDestination
import org.solovyev.android.calculator.ui.compose.CalculatorTopBar
import org.solovyev.android.calculator.ui.compose.CalculatorComposeViewModel
import org.solovyev.android.calculator.ui.compose.ModernModeBottomBar
import org.solovyev.android.calculator.ui.compose.RateUsDialog
import org.solovyev.android.calculator.ui.compose.components.CalculatorKeyboard
import org.solovyev.android.calculator.ui.compose.components.CalculatorScreen
import org.solovyev.android.calculator.ui.compose.components.KeyboardMode
import org.solovyev.android.calculator.ui.compose.converter.ConverterDialog
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme
import org.solovyev.android.calculator.buttons.CppSpecialButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import org.solovyev.android.calculator.history.History
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import javax.inject.Inject

@AndroidEntryPoint
class CalculatorActivity : BaseActivity(R.string.cpp_app_name) {

    @Inject
    lateinit var history: History

    @Inject
    lateinit var launcher: ActivityLauncher

    @Inject
    lateinit var startupHelper: StartupHelper

    @Inject
    lateinit var clipboard: Clipboard

    private var useBackAsPrevious = false
    private var shouldShowRateUs = false

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    @Composable
    override fun Content() {
        // Determine initial screen based on wizard state
        val startScreen = rememberSaveable {
            val pending = startupHelper.getPendingWizard(this@CalculatorActivity)
            if (pending != null) {
                "wizard:${pending.first}:${pending.second ?: ""}"
            } else {
                "calculator"
            }
        }
        var currentScreen by rememberSaveable { mutableStateOf(startScreen) }

        // Collect navigation events from launcher
        LaunchedEffect(launcher) {
            launcher.navigationFlow.collect { key ->
                currentScreen = when (key) {
                    is Settings -> "settings"
                    is HistoryKey -> "history"
                    is About -> "about"
                    is Calculator -> "calculator"
                    is Wizard -> "wizard:${key.flowName}:${key.startStep ?: ""}"
                    else -> "calculator"
                }
            }
        }

        // Render current screen
        when {
            currentScreen == "calculator" -> {
                CalculatorDestination(
                    onOpenSettings = { launcher.showSettings() },
                    onOpenHistory = { launcher.showHistory() },
                    onOpenAbout = { launcher.showAbout() }
                )
            }

            currentScreen == "settings" -> {
                SettingsDestination(onBack = { currentScreen = "calculator" })
            }

            currentScreen == "history" -> {
                HistoryDestination(
                    onBack = { currentScreen = "calculator" },
                    onFinish = { currentScreen = "calculator" }
                )
            }

            currentScreen == "about" -> {
                AboutDestination(onBack = { currentScreen = "calculator" })
            }

            currentScreen.startsWith("wizard:") -> {
                val parts = currentScreen.removePrefix("wizard:").split(":", limit = 2)
                WizardDestination(
                    flowName = parts[0],
                    startStep = parts.getOrNull(1)?.takeIf { it.isNotEmpty() },
                    onFinishCallback = { currentScreen = "calculator" }
                )
            }
        }
    }

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    @Composable
    private fun SettingsDestination(onBack: () -> Unit) {
        val themePreference by appPreferences.settings.theme.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getThemeBlocking()
        )
        var settingsDestination by rememberSaveable {
            mutableStateOf(org.solovyev.android.calculator.preferences.SettingsDestination.MAIN)
        }

        CalculatorTheme(theme = themePreference) {
            BackHandler(enabled = settingsDestination != org.solovyev.android.calculator.preferences.SettingsDestination.MAIN) {
                settingsDestination = org.solovyev.android.calculator.preferences.SettingsDestination.MAIN
            }

            BackHandler(enabled = settingsDestination == org.solovyev.android.calculator.preferences.SettingsDestination.MAIN) {
                onBack()
            }

            val titleResId = when (settingsDestination) {
                org.solovyev.android.calculator.preferences.SettingsDestination.MAIN -> R.string.cpp_settings
                org.solovyev.android.calculator.preferences.SettingsDestination.NUMBER_FORMAT -> R.string.cpp_number_format
                org.solovyev.android.calculator.preferences.SettingsDestination.APPEARANCE -> R.string.cpp_appearance
                org.solovyev.android.calculator.preferences.SettingsDestination.ONSCREEN -> R.string.cpp_floating_calculator
                org.solovyev.android.calculator.preferences.SettingsDestination.WIDGET -> R.string.cpp_widget
                org.solovyev.android.calculator.preferences.SettingsDestination.OTHER -> R.string.cpp_other
            }

            val canGoBack = settingsDestination != org.solovyev.android.calculator.preferences.SettingsDestination.MAIN

            androidx.compose.material3.Scaffold(
                topBar = {
                    androidx.compose.material3.TopAppBar(
                        title = { Text(text = androidx.compose.ui.res.stringResource(titleResId)) },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    if (canGoBack) {
                                        settingsDestination =
                                            org.solovyev.android.calculator.preferences.SettingsDestination.MAIN
                                    } else {
                                        onBack()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = androidx.compose.ui.res.stringResource(R.string.cpp_back)
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                androidx.compose.material3.Surface(
                    modifier = androidx.compose.ui.Modifier.padding(innerPadding),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    org.solovyev.android.calculator.preferences.SettingsScreen(
                        destination = settingsDestination,
                        adFreePurchased = false, // TODO: Wire up billing
                        onNavigate = { settingsDestination = it },
                        onStartWizard = { /* Navigate to wizard via launcher */ },
                        onReportBug = { /* Open email intent */ },
                        onOpenAbout = { launcher.showAbout() },
                        onSupportProject = { /* Open purchase dialog */ }
                    )
                }
            }
        }
    }

    @Composable
    private fun HistoryDestination(onBack: () -> Unit, onFinish: () -> Unit) {
        val themePreference by appPreferences.settings.theme.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getThemeBlocking()
        )
        val recent = history.getRecent(forUi = true)
        val saved = history.getSaved()

        CalculatorTheme(theme = themePreference) {
            org.solovyev.android.calculator.ui.compose.history.HistoryScreen(
                recent = recent,
                saved = saved,
                onUse = { state ->
                    history.applyHistoryState(state)
                    onFinish()
                },
                onCopyExpression = { state ->
                    clipboard.setText(org.solovyev.android.calculator.history.HistoryTextFormatter.format(state))
                },
                onCopyResult = { state ->
                    clipboard.setText(state.display.text.ifEmpty { "" })
                },
                onSave = { history.updateSaved(it) },
                onEdit = { history.updateSaved(it) },
                onDelete = { history.removeSaved(it) },
                onClearRecent = { history.clearRecent() },
                onClearSaved = { history.clearSaved() },
                onBack = onBack
            )
        }
    }

    @Composable
    private fun AboutDestination(onBack: () -> Unit) {
        val themePreference by appPreferences.settings.theme.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getThemeBlocking()
        )
        CalculatorTheme(theme = themePreference) {
            org.solovyev.android.calculator.ui.compose.about.AboutScreen(onBack = onBack)
        }
    }

    @Composable
    private fun CalculatorDestination(
        onOpenSettings: () -> Unit,
        onOpenHistory: () -> Unit,
        onOpenAbout: () -> Unit
    ) {
        val context = LocalContext.current
        val viewModel: CalculatorComposeViewModel = hiltViewModel()
        val displayState by viewModel.displayState.collectAsStateWithLifecycle()
        val editorState by viewModel.editorState.collectAsStateWithLifecycle()
        var showConverter by rememberSaveable { mutableStateOf(false) }
        var showRateUs by rememberSaveable { mutableStateOf(false) }

        // Show rate us dialog if startup helper flagged it
        LaunchedEffect(Unit) {
            if (shouldShowRateUs) {
                showRateUs = true
                shouldShowRateUs = false
            }
        }

        val modeState by appPreferences.settings.mode.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getModeBlocking()
        )
        val angleUnitState by appPreferences.settings.angleUnit.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getAngleUnitBlocking()
        )
        val numeralBaseState by appPreferences.settings.numeralBase.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getNumeralBaseBlocking()
        )
        val themePreference by appPreferences.settings.theme.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getThemeBlocking()
        )
        val highlightExpressions by appPreferences.settings.highlightExpressions.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getHighlightExpressionsBlocking()
        )
        val highContrast by appPreferences.settings.highContrast.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getHighContrastBlocking()
        )
        val vibrateOnKeypress by appPreferences.settings.vibrateOnKeypress.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.vibrateOnKeypressBlocking()
        )
        val keyboardMode = when (modeState) {
            Preferences.Gui.Mode.engineer -> KeyboardMode.ENGINEER
            Preferences.Gui.Mode.simple -> KeyboardMode.SIMPLE
            Preferences.Gui.Mode.modern -> KeyboardMode.MODERN
        }
        val isModernMode = modeState == Preferences.Gui.Mode.modern

        CalculatorTheme(theme = themePreference) {
            CalculatorScreen(
                displayState = displayState,
                editorState = editorState,
                onEditorTextChange = viewModel::onEditorTextChange,
                onEditorSelectionChange = viewModel::onEditorSelectionChange,
                highlightExpressions = highlightExpressions,
                highContrast = highContrast,
                hapticsEnabled = vibrateOnKeypress,
                keyboard = { modifier ->
                    CalculatorKeyboard(
                        mode = keyboardMode,
                        actions = viewModel,
                        modifier = modifier
                    )
                },
                onEquals = viewModel::onEquals,
                onSimplify = { viewModel.onSpecialClick(CppSpecialButton.simplify.action) },
                onPlot = { viewModel.onSpecialClick(CppSpecialButton.plot_add.glyph.toString()) },
                overlayContent = {
                    if (!isModernMode) {
                        // Standard top bar for engineer/simple modes
                        CalculatorTopBar(
                            mode = modeState,
                            angleUnit = angleUnitState,
                            numeralBase = numeralBaseState,
                            onModeChange = ::setMode,
                            onAngleUnitChange = ::setAngleUnit,
                            onNumeralBaseChange = ::setNumeralBase,
                            onOpenSettings = onOpenSettings,
                            onOpenHistory = onOpenHistory,
                            onOpenPlotter = launcher::showPlotter,
                            onOpenConverter = { showConverter = true },
                            onOpenAbout = onOpenAbout,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                bottomBar = {
                    if (isModernMode) {
                        // Modern mode: floating toolbar at bottom
                        ModernModeBottomBar(
                            viewModel = viewModel,
                            onOpenSettings = onOpenSettings,
                            onOpenHistory = onOpenHistory,
                            onOpenConverter = { showConverter = true }
                        )
                    }
                }
            )

            if (showRateUs) {
                RateUsDialog(
                    onDismissRequest = {
                        showRateUs = false
                        startupHelper.markRateUsShown()
                    },
                    onRateClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data =
                                    "https://play.google.com/store/apps/details?id=org.solovyev.android.calculator".toUri()
                            }
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                        }
                    }
                )
            }
        }

        if (showConverter) {
            CalculatorTheme(theme = themePreference) {
                ConverterDialog(onDismissRequest = { showConverter = false })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        useBackAsPrevious = appPreferences.settings.getUseBackAsPreviousBlocking()
        if (savedInstanceState == null) {
            shouldShowRateUs = startupHelper.onMainActivityOpened(this)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.repeatCount == 0 && useBackAsPrevious) {
            history.undo()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        launcher.setActivity(this)
        restartIfModeChanged()
    }

    override fun onPause() {
        launcher.clearActivity(this)
        super.onPause()
    }

    private fun setMode(mode: Preferences.Gui.Mode) {
        lifecycleScope.launch {
            appPreferences.settings.setMode(mode)
            restartIfModeChanged()
        }
    }

    private fun setAngleUnit(angleUnit: AngleUnit) {
        lifecycleScope.launch {
            appPreferences.settings.setAngleUnit(angleUnit)
        }
    }

    private fun setNumeralBase(numeralBase: NumeralBase) {
        lifecycleScope.launch {
            appPreferences.settings.setNumeralBase(numeralBase)
        }
    }

    private companion object
}
