package org.solovyev.android.calculator.wizard

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme
import org.solovyev.android.wizard.ListWizardFlow
import org.solovyev.android.wizard.WizardUi
import org.solovyev.android.wizard.Wizards
import org.solovyev.android.wizard.WizardsAware
import org.solovyev.android.wizard.WizardStep
import javax.inject.Inject

@AndroidEntryPoint
class WizardActivity : BaseActivity(0), WizardsAware {

    private val wizardUi: WizardUi<WizardActivity> = WizardUi(this, this, 0)
    private var currentStep: WizardStep? = null

    @Inject
    override lateinit var wizards: Wizards

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wizardUi.onCreate(savedInstanceState)
    }

    @Composable
    override fun Content() {
        val themePreference by appPreferences.settings.theme.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getThemeBlocking()
        )
        CalculatorTheme(theme = themePreference) {
            WizardContent()
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun WizardContent(viewModel: WizardComposeViewModel = hiltViewModel()) {
        val flow = wizardUi.flow as ListWizardFlow
        val wizardName = wizardUi.getWizard().name
        val settingsState by viewModel.state.collectAsStateWithLifecycle()
        var stepName by rememberSaveable { mutableStateOf(wizardUi.step?.name ?: flow.firstStep.name) }
        var showFinishConfirm by remember { mutableStateOf(false) }
        val step = flow.getStepByName(stepName) ?: flow.firstStep

        LaunchedEffect(stepName) {
            wizardUi.step = step
            wizardUi.getWizard().saveLastStep(step)
            WizardUi.tryPutStep(intent, step)
            currentStep = step
        }

        BackHandler {
            if (flow.getPrevStep(step) != null) {
                stepName = flow.getPrevStep(step)?.name ?: stepName
            } else {
                val confirmed = wizardName == CalculatorWizards.RELEASE_NOTES ||
                    wizardName == CalculatorWizards.DEFAULT_WIZARD_FLOW
                if (confirmed) {
                    finishWizardAbruptly()
                } else {
                    showFinishConfirm = true
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.cpp_wizard_title)) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                val prev = flow.getPrevStep(step)
                                if (prev != null) {
                                    stepName = prev.name
                                } else {
                                    val confirmed = wizardName == CalculatorWizards.RELEASE_NOTES ||
                                        wizardName == CalculatorWizards.DEFAULT_WIZARD_FLOW
                                    if (confirmed) {
                                        finishWizardAbruptly()
                                    } else {
                                        showFinishConfirm = true
                                    }
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
                WizardScreen(
                    flow = flow,
                    step = step,
                    wizardName = wizardName,
                    settingsState = settingsState,
                    onModeChange = viewModel::setMode,
                    onThemeChange = viewModel::setTheme,
                    onShowAppIconChange = viewModel::setShowAppIcon,
                    onNext = {
                        val next = flow.getNextStep(step)
                        if (next != null) {
                            stepName = next.name
                        }
                    },
                    onPrev = {
                        val prev = flow.getPrevStep(step)
                        if (prev != null) {
                            stepName = prev.name
                        }
                    },
                    onFinish = { finishWizard() },
                    onFinishAbruptly = {
                        val confirmed = wizardName == CalculatorWizards.RELEASE_NOTES ||
                            wizardName == CalculatorWizards.DEFAULT_WIZARD_FLOW
                        if (confirmed) {
                            finishWizardAbruptly()
                        } else {
                            showFinishConfirm = true
                        }
                    }
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
                            finishWizardAbruptly()
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

    fun finishWizardAbruptly() {
        wizardUi.finishWizardAbruptly()
        finish()
    }

    fun finishWizard() {
        wizardUi.finishWizard()
        finish()
    }

    fun getWizard() = wizardUi.getWizard()

    fun getFlow() = wizardUi.flow

    fun canGoNext(): Boolean {
        val step = currentStep ?: wizardUi.step ?: wizardUi.flow.firstStep
        return (wizardUi.flow as? ListWizardFlow)?.getNextStep(step) != null
    }

    fun canGoPrev(): Boolean {
        val step = currentStep ?: wizardUi.step ?: wizardUi.flow.firstStep
        return (wizardUi.flow as? ListWizardFlow)?.getPrevStep(step) != null
    }

    fun goNext() {
        val step = currentStep ?: wizardUi.step ?: wizardUi.flow.firstStep
        val next = (wizardUi.flow as? ListWizardFlow)?.getNextStep(step) ?: return
        wizardUi.step = next
        currentStep = next
        wizardUi.getWizard().saveLastStep(next)
    }

    fun goPrev() {
        val step = currentStep ?: wizardUi.step ?: wizardUi.flow.firstStep
        val prev = (wizardUi.flow as? ListWizardFlow)?.getPrevStep(step) ?: return
        wizardUi.step = prev
        currentStep = prev
        wizardUi.getWizard().saveLastStep(prev)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        wizardUi.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        wizardUi.onPause()
    }
}
