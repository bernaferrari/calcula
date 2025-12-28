package org.solovyev.android.calculator.wizard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.testutils.MainDispatcherRule
import org.solovyev.android.wizard.ListWizardFlow
import org.solovyev.android.wizard.Wizard
import org.solovyev.android.wizard.WizardFlow
import org.solovyev.android.wizard.WizardStep
import org.solovyev.android.wizard.Wizards

@RunWith(RobolectricTestRunner::class)
class CalculatorWizardActivityTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun testStartWizardUsesFirstStep() {
        val flow = ListWizardFlow(listOf(CalculatorWizardStep.WELCOME, CalculatorWizardStep.CHOOSE_MODE))
        val wizard = FakeWizard("flow", flow)
        val viewModel = WizardComposeViewModel(
            AppPreferences(RuntimeEnvironment.application),
            FakeWizards(wizard)
        )

        viewModel.startWizard("flow", null)
        val state = viewModel.wizardState.value
        assertNotNull(state)
        assertEquals(flow.firstStep, state?.step)
    }

    @Test
    fun testStepNavigationUpdatesWizardState() {
        val flow = ListWizardFlow(
            listOf(CalculatorWizardStep.WELCOME, CalculatorWizardStep.CHOOSE_MODE, CalculatorWizardStep.LAST)
        )
        val wizard = FakeWizard("flow", flow)
        val viewModel = WizardComposeViewModel(
            AppPreferences(RuntimeEnvironment.application),
            FakeWizards(wizard)
        )

        viewModel.startWizard("flow", CalculatorWizardStep.WELCOME.name)
        viewModel.nextStep()
        assertEquals(CalculatorWizardStep.CHOOSE_MODE, viewModel.wizardState.value?.step)
        assertEquals(CalculatorWizardStep.CHOOSE_MODE.name, wizard.lastSavedStepName)

        viewModel.nextStep()
        assertEquals(CalculatorWizardStep.LAST, viewModel.wizardState.value?.step)
        assertEquals(CalculatorWizardStep.LAST.name, wizard.lastSavedStepName)

        viewModel.prevStep()
        assertEquals(CalculatorWizardStep.CHOOSE_MODE, viewModel.wizardState.value?.step)
    }

    @Test
    fun testFinishWizardMarksFinished() {
        val flow = ListWizardFlow(listOf(CalculatorWizardStep.WELCOME, CalculatorWizardStep.LAST))
        val wizard = FakeWizard("flow", flow)
        val viewModel = WizardComposeViewModel(
            AppPreferences(RuntimeEnvironment.application),
            FakeWizards(wizard)
        )

        viewModel.startWizard("flow", CalculatorWizardStep.LAST.name)
        viewModel.finishWizard()
        assertTrue(wizard.isFinished)
    }

    private class FakeWizards(private val wizard: Wizard) : Wizards {
        override fun getWizard(name: String?, arguments: android.os.Bundle?): Wizard = wizard
    }

    private class FakeWizard(
        override val name: String,
        override val flow: WizardFlow
    ) : Wizard {
        private var lastStep: WizardStep? = null
        private var finished = false

        override val lastSavedStepName: String?
            get() = lastStep?.name

        override val isFinished: Boolean
            get() = finished

        override val isStarted: Boolean
            get() = lastStep != null

        override fun saveLastStep(step: WizardStep) {
            lastStep = step
        }

        override fun saveFinished(step: WizardStep, forceFinish: Boolean) {
            finished = forceFinish || flow.getNextStep(step) == null
        }
    }
}
