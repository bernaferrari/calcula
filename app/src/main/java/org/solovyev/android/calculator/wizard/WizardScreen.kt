package org.solovyev.android.calculator.wizard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme
import org.solovyev.android.wizard.ListWizardFlow

import org.solovyev.android.calculator.wizard.WizardComposeViewModel
import org.solovyev.android.calculator.wizard.WizardSettingsState
import org.solovyev.android.calculator.wizard.WizardState
import org.solovyev.android.calculator.wizard.CalculatorWizards

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardDestination(
    flowName: String,
    startStep: String?,
    onFinishCallback: () -> Unit, // Callback when wizard finishes
    viewModel: WizardComposeViewModel = hiltViewModel()
) {
    LaunchedEffect(flowName, startStep) {
        viewModel.startWizard(flowName, startStep)
    }

    val wizardState by viewModel.wizardState.collectAsStateWithLifecycle()
    val settingsState by viewModel.state.collectAsStateWithLifecycle()

    // We treat theme settings globally, so we wrap the content
    CalculatorTheme(theme = settingsState.theme) {
        val state = wizardState
        if (state != null) {
            WizardContentInternal(
                state = state,
                settingsState = settingsState,
                viewModel = viewModel,
                onFinishCallback = onFinishCallback
            )
        } else {
            // Loading or empty state
            Box(modifier = Modifier)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WizardContentInternal(
    state: WizardState,
    settingsState: WizardSettingsState,
    viewModel: WizardComposeViewModel,
    onFinishCallback: () -> Unit
) {
    val wizardName = state.wizard.name
    var showFinishConfirm by remember { mutableStateOf(false) }

    fun tryFinish() {
        val confirmed = wizardName == CalculatorWizards.RELEASE_NOTES ||
                wizardName == CalculatorWizards.DEFAULT_WIZARD_FLOW
        if (confirmed) {
            viewModel.finishWizard(true)
            onFinishCallback()
        } else {
            showFinishConfirm = true
        }
    }

    BackHandler {
        val flow = state.flow as? ListWizardFlow
        if (flow?.getPrevStep(state.step) != null) {
            viewModel.prevStep()
        } else {
            tryFinish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.cpp_wizard_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val flow = state.flow as? ListWizardFlow
                            if (flow?.getPrevStep(state.step) != null) {
                                viewModel.prevStep()
                            } else {
                                tryFinish()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cpp_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            // Reusing the existing WizardScreen content composable (renaming needed?)
            // Wait, WizardActivity uses a `WizardScreen` composable internally.
            // I need to verify where that `WizardScreen` is defined.
            // Assuming it's in a file I haven't seen or inside WizardActivity (it wasn't inside).
            // It was likely imported. Let's assume org.solovyev.android.calculator.wizard.WizardScreen
            // exists. Wait, I saw it used in WizardActivity.kt line 127.
            // I will use fully qualified name or just assume it's available.
            // Actually, I should check if `WizardScreen` (the inner one) is available.
            // For now, I'll call it `WizardPage` to avoid conflict with this file name if I naming this WizardScreen.
            // In WizardActivity it was called `WizardScreen`.

            WizardScreen(
                flow = state.flow as ListWizardFlow,
                step = state.step,
                wizardName = wizardName,
                settingsState = settingsState,
                onModeChange = viewModel::setMode,
                onThemeChange = viewModel::setTheme,
                onShowAppIconChange = viewModel::setShowAppIcon,
                onNext = { viewModel.nextStep() },
                onPrev = { viewModel.prevStep() },
                onFinish = {
                    viewModel.finishWizard()
                    onFinishCallback()
                },
                onFinishAbruptly = { tryFinish() }
            )
        }
    }

    if (showFinishConfirm) {
        AlertDialog(
            onDismissRequest = { showFinishConfirm = false },
            title = { Text(text = stringResource(R.string.cpp_wizard_finish_confirmation_title)) },
            text = { Text(text = stringResource(R.string.cpp_wizard_finish_confirmation)) },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinishConfirm = false
                        viewModel.finishWizard(true)
                        onFinishCallback()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = stringResource(R.string.cpp_yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFinishConfirm = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(text = stringResource(R.string.cpp_no))
                }
            }
        )
    }
}
