package org.solovyev.android.calculator.wizard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.solovyev.android.calculator.wizard.CalculatorWizardStep.choose_mode
import org.solovyev.android.calculator.wizard.CalculatorWizardStep.last
import org.solovyev.android.calculator.wizard.CalculatorWizardStep.welcome
import org.solovyev.android.wizard.Wizard
import org.solovyev.android.wizard.Wizards

@Config(manifest = Config.NONE)
@RunWith(value = RobolectricTestRunner::class)
class CalculatorWizardTest {

    private lateinit var wizards: Wizards

    private lateinit var wizard: Wizard

    private lateinit var defaultWizard: Wizard

    @Before
    @Throws(Exception::class)
    fun setUp() {
        wizards = CalculatorWizards(RuntimeEnvironment.application)
        wizard = wizards.getWizard(null)
        defaultWizard = wizards.getWizard(CalculatorWizards.DEFAULT_WIZARD_FLOW)
    }

    @Test
    @Throws(Exception::class)
    fun testDefaultFlowShouldNotContainWelcomeAndLastSteps() {
        val flow = defaultWizard.flow
        assertNull(flow.getStepByName(welcome.name))
        assertNull(flow.getStepByName(last.name))
    }

    @Test
    @Throws(Exception::class)
    fun testFirstTimeFlowShouldContainWelcomeAndLastSteps() {
        val flow = wizard.flow
        assertNotNull(flow.getStepByName(welcome.name))
        assertNotNull(flow.getStepByName(last.name))
    }

    @Test
    @Throws(Exception::class)
    fun testShouldThrowExceptionIfUnknownWizard() {
        try {
            wizards.getWizard("testtesttesttesttest")
            fail()
        } catch (e: IllegalArgumentException) {
            // ok
        }
    }

    @Test
    @Throws(Exception::class)
    fun testShouldReturnWizard() {
        assertNotNull(wizards.getWizard(CalculatorWizards.FIRST_TIME_WIZARD))
        assertNotNull(wizards.getWizard(CalculatorWizards.DEFAULT_WIZARD_FLOW))
    }

    @Test
    @Throws(Exception::class)
    fun testShouldSaveWizardIsFinishedWhenNotLastStepAndForce() {
        assertFalse(wizard.isFinished)
        wizard.saveFinished(CalculatorWizardStep.drag_button, true)

        assertTrue(wizard.isFinished)
    }

    @Test
    @Throws(Exception::class)
    fun testShouldNotSaveWizardIsFinishedWhenNotLastStepAndNotForce() {
        assertFalse(wizard.isFinished)
        wizard.saveFinished(CalculatorWizardStep.drag_button, false)

        assertFalse(wizard.isFinished)
    }

    @Test
    @Throws(Exception::class)
    fun testShouldSaveWizardIsFinishedWhenLastStep() {
        assertFalse(wizard.isFinished)
        wizard.saveFinished(CalculatorWizardStep.last, false)

        assertTrue(wizard.isFinished)
    }

    @Test
    @Throws(Exception::class)
    fun testShouldSaveLastWizardStep() {
        assertFalse(wizard.isStarted)
        assertNull(wizard.lastSavedStepName)

        wizard.saveLastStep(choose_mode)
        assertTrue(wizard.isStarted)
        assertEquals(choose_mode.name, wizard.lastSavedStepName)
    }
}
