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
import org.solovyev.android.calculator.history.History
import org.solovyev.android.calculator.ui.compose.CalculatorComposeViewModel
import org.solovyev.android.calculator.ui.compose.CalculatorTopBar
import org.solovyev.android.calculator.ui.compose.RateUsDialog
import org.solovyev.android.calculator.ui.compose.components.CalculatorKeyboard
import org.solovyev.android.calculator.ui.compose.components.CalculatorScreen
import org.solovyev.android.calculator.ui.compose.components.KeyboardMode
import org.solovyev.android.calculator.ui.compose.converter.ConverterDialog
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme
import org.solovyev.android.calculator.buttons.CppSpecialButton
import javax.inject.Inject

@AndroidEntryPoint
class CalculatorActivity : BaseActivity(R.string.cpp_app_name) {

    @Inject
    lateinit var history: History

    @Inject
    lateinit var launcher: ActivityLauncher

    @Inject
    lateinit var startupHelper: StartupHelper

    private var useBackAsPrevious = false
    private var shouldShowRateUs = false

    @Composable
    override fun Content() {
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
        val keyboardMode = if (modeState == Preferences.Gui.Mode.engineer) {
            KeyboardMode.ENGINEER
        } else {
            KeyboardMode.SIMPLE
        }
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
                    CalculatorTopBar(
                        mode = modeState,
                        angleUnit = angleUnitState,
                        numeralBase = numeralBaseState,
                        onModeChange = ::setMode,
                        onAngleUnitChange = ::setAngleUnit,
                        onNumeralBaseChange = ::setNumeralBase,
                        onOpenSettings = launcher::showSettings,
                        onOpenHistory = launcher::showHistory,
                        onOpenPlotter = launcher::showPlotter,
                        onOpenConverter = { showConverter = true },
                        onOpenAbout = launcher::showAbout,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                                data = "https://play.google.com/store/apps/details?id=org.solovyev.android.calculator".toUri()
                            }
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                        }
                    }
                )
            }
        }

        if (showConverter) {
            ConverterDialog(onDismissRequest = { showConverter = false })
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
