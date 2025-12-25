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

import android.os.Build
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.solovyev.android.wizard.WizardUi
import java.lang.reflect.Field

@Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
@RunWith(RobolectricTestRunner::class)
class OnScreenCalculatorWizardStepTest {

    private lateinit var fragment: OnScreenCalculatorWizardStep

    private lateinit var activity: WizardActivity

    private lateinit var controller: ActivityController<WizardActivity>
    private lateinit var uiField: Field

    @Before
    @Throws(Exception::class)
    fun setUp() {
        uiField = WizardActivity::class.java.getDeclaredField("wizardUi")
        uiField.isAccessible = true

        createActivity()
        setFragment()
    }

    @Throws(IllegalAccessException::class)
    private fun getWizardUi(): WizardUi {
        return uiField.get(activity) as WizardUi
    }

    private fun createActivity() {
        controller = Robolectric.buildActivity(WizardActivity::class.java).create().start().resume()
        activity = controller.get()
    }

    @Throws(IllegalAccessException::class)
    private fun setFragment() {
        getWizardUi().step = CalculatorWizardStep.on_screen_calculator
        activity.supportFragmentManager.executePendingTransactions()
        fragment = activity.supportFragmentManager.findFragmentByTag(CalculatorWizardStep.on_screen_calculator.fragmentTag) as OnScreenCalculatorWizardStep
    }

    @Test
    @Throws(Exception::class)
    fun testShouldRestoreStateOnRestart() {
        fragment.checkbox.isChecked = true
        controller.restart()
        assertTrue(fragment.checkbox.isChecked)

        fragment.checkbox.isChecked = false
        controller.restart()
        assertFalse(fragment.checkbox.isChecked)
    }
}
