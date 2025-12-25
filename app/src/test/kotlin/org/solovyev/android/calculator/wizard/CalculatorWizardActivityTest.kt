/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.wizard

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.solovyev.android.calculator.wizard.CalculatorWizardStep.choose_mode
import org.solovyev.android.wizard.WizardUi
import org.solovyev.android.wizard.Wizards
import java.lang.reflect.Field

@RunWith(value = RobolectricTestRunner::class)
class CalculatorWizardActivityTest {

    private lateinit var controller: ActivityController<WizardActivity>
    private lateinit var activity: WizardActivity
    private lateinit var wizards: Wizards
    private lateinit var uiField: Field

    @Before
    @Throws(Exception::class)
    fun setUp() {
        controller = Robolectric.buildActivity(WizardActivity::class.java)
        activity = controller.get()
        wizards = CalculatorWizards(RuntimeEnvironment.application)
        activity.setWizards(wizards)
        controller.create()

        uiField = WizardActivity::class.java.getDeclaredField("wizardUi")
        uiField.isAccessible = true
    }

    @Test
    @Throws(Exception::class)
    fun testShouldBeFirstTimeWizardByDefault() {
        assertEquals(CalculatorWizards.FIRST_TIME_WIZARD, getWizardUi().wizard.name)
    }

    @Throws(IllegalAccessException::class)
    private fun getWizardUi(): WizardUi {
        return uiField.get(activity) as WizardUi
    }

    @Test
    @Throws(Exception::class)
    fun testShouldBeFirstStep() {
        assertNotNull(getWizardUi().step)
        assertEquals(getWizardUi().flow.firstStep, getWizardUi().step)
    }

    @Test
    @Throws(Exception::class)
    fun testShouldSaveState() {
        getWizardUi().step = choose_mode

        val outState = Bundle()
        controller.saveInstanceState(outState)

        controller = Robolectric.buildActivity(WizardActivity::class.java)
        controller.create(outState)

        activity = controller.get()
        assertNotNull(getWizardUi().flow)
        assertEquals(CalculatorWizards.FIRST_TIME_WIZARD, getWizardUi().wizard.name)
        assertNotNull(getWizardUi().step)
        assertEquals(choose_mode, getWizardUi().step)
    }

    @Test
    @Throws(Exception::class)
    fun testCreate() {
        val intent = Intent()
        intent.setClass(activity, WizardActivity::class.java)
        intent.putExtra("flow", CalculatorWizards.DEFAULT_WIZARD_FLOW)
        controller = Robolectric.buildActivity(WizardActivity::class.java, intent)
        controller.create()
        activity = controller.get()
        assertEquals(CalculatorWizards.DEFAULT_WIZARD_FLOW, getWizardUi().wizard.name)
        assertEquals(getWizardUi().flow.firstStep, getWizardUi().step)

        val outState1 = Bundle()
        controller.saveInstanceState(outState1)

        controller = Robolectric.buildActivity(WizardActivity::class.java)
        activity = controller.get()
        controller.create(outState1)
        assertEquals(CalculatorWizards.DEFAULT_WIZARD_FLOW, getWizardUi().wizard.name)
        assertEquals(getWizardUi().flow.firstStep, getWizardUi().step)
    }

    @Test
    @Throws(Exception::class)
    fun testShouldAddFirstFragment() {
        controller.start().resume()

        val fm: FragmentManager = activity.supportFragmentManager
        val f = fm.findFragmentByTag(CalculatorWizardStep.welcome.fragmentTag)
        assertNotNull(f)
        assertTrue(f!!.isAdded)
    }

    @Test
    @Throws(Exception::class)
    fun testShouldAddStepFragment() {
        controller.start().resume()

        val fm: FragmentManager = activity.supportFragmentManager

        getWizardUi().step = choose_mode

        val f = fm.findFragmentByTag(choose_mode.fragmentTag)
        assertNotNull(f)
        assertTrue(f!!.isAdded)
    }

    @Test
    @Throws(Exception::class)
    fun testSetStep() {
        getWizardUi().step = choose_mode
        assertEquals(choose_mode, getWizardUi().step)
    }

    @Test
    @Throws(Exception::class)
    fun testShouldStartWizardActivityAfterStart() {
        val shadowActivity = Shadows.shadowOf(controller.get())
        WizardUi.startWizard(activity.wizards, CalculatorWizards.DEFAULT_WIZARD_FLOW, RuntimeEnvironment.application)
        assertNotNull(shadowActivity.nextStartedActivity)
    }

    @Test
    @Throws(Exception::class)
    fun testTitleShouldBeSet() {
        getWizardUi().step = choose_mode
        assertEquals(activity.getString(choose_mode.titleResId), activity.title.toString())
    }

    @Throws(IllegalAccessException::class)
    private fun setLastStep() {
        getWizardUi().step = CalculatorWizardStep.values()[CalculatorWizardStep.values().size - 1]
    }

    @Throws(IllegalAccessException::class)
    private fun setFirstStep() {
        getWizardUi().step = CalculatorWizardStep.values()[0]
    }

    @Test
    @Throws(Exception::class)
    fun testShouldSaveLastWizardStateOnPause() {
        val wizard = wizards.getWizard(getWizardUi().wizard.name)
        assertNull(wizard.lastSavedStepName)
        getWizardUi().step = CalculatorWizardStep.drag_button
        activity.onPause()
        assertEquals(CalculatorWizardStep.drag_button.name, wizard.lastSavedStepName)
    }

    @Test
    @Throws(Exception::class)
    fun testShouldSaveFinishedIfLastStep() {
        val wizard = wizards.getWizard(getWizardUi().wizard.name)
        assertFalse(wizard.isFinished)
        setLastStep()
        getWizardUi().finishWizard()
        assertTrue(wizard.isFinished)
    }

    @Test
    @Throws(Exception::class)
    fun testShouldNotSaveFinishedIfNotLastStep() {
        val wizard = wizards.getWizard(getWizardUi().wizard.name)
        assertFalse(wizard.isFinished)
        setFirstStep()
        getWizardUi().finishWizard()
        assertFalse(wizard.isFinished)
    }
}
