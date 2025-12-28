package org.solovyev.android.calculator.wizard

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
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
        assertNull(flow.getStepByName(CalculatorWizardStep.WELCOME.name))
        assertNull(flow.getStepByName(CalculatorWizardStep.LAST.name))
    }

    @Test
    @Throws(Exception::class)
    fun testFirstTimeFlowShouldContainWelcomeAndLastSteps() {
        val flow = wizard.flow
        assertNotNull(flow.getStepByName(CalculatorWizardStep.WELCOME.name))
        assertNotNull(flow.getStepByName(CalculatorWizardStep.LAST.name))
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
    fun testShouldReturnWizard() {
        assertNotNull(wizards.getWizard(CalculatorWizards.FIRST_TIME_WIZARD))
        assertNotNull(wizards.getWizard(CalculatorWizards.DEFAULT_WIZARD_FLOW))
    }
}
