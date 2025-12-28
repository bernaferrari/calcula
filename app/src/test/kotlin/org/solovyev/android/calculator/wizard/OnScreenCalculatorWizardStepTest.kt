package org.solovyev.android.calculator.wizard

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.testutils.MainDispatcherRule
import org.solovyev.android.wizard.Wizard
import org.solovyev.android.wizard.WizardFlow
import org.solovyev.android.wizard.WizardStep
import org.solovyev.android.wizard.Wizards

@RunWith(RobolectricTestRunner::class)
class OnScreenCalculatorWizardStepTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun testShowAppIconUpdatesState() = runBlocking {
        val appPreferences = AppPreferences(RuntimeEnvironment.application)
        val viewModel = WizardComposeViewModel(
            appPreferences,
            FakeWizards(FakeWizard("flow"))
        )

        viewModel.setShowAppIcon(false)
        val state = viewModel.state.first { !it.showAppIcon }
        assertFalse(state.showAppIcon)

        viewModel.setShowAppIcon(true)
        val updated = viewModel.state.first { it.showAppIcon }
        assertTrue(updated.showAppIcon)
    }

    private class FakeWizards(private val wizard: Wizard) : Wizards {
        override fun getWizard(name: String?, arguments: android.os.Bundle?): Wizard = wizard
    }

    private class FakeWizard(override val name: String) : Wizard {
        override val lastSavedStepName: String? = null
        override val isFinished: Boolean = false
        override val isStarted: Boolean = false
        override val flow: WizardFlow = object : WizardFlow {
            override fun getStepByName(name: String): WizardStep? = null
            override fun getNextStep(step: WizardStep): WizardStep? = null
            override fun getPrevStep(step: WizardStep): WizardStep? = null
            override val firstStep: WizardStep
                get() = CalculatorWizardStep.WELCOME
        }

        override fun saveLastStep(step: WizardStep) = Unit

        override fun saveFinished(step: WizardStep, forceFinish: Boolean) = Unit
    }
}
