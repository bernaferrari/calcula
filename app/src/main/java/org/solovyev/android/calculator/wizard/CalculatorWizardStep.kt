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

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.solovyev.android.calculator.R
import org.solovyev.android.wizard.WizardStep

enum class CalculatorWizardStep(
    override val fragmentClass: Class<out Fragment>,
    override val titleResId: Int,
    override val nextButtonTitleResId: Int = R.string.cpp_wizard_next
) : WizardStep {

    WELCOME(WelcomeWizardStep::class.java, R.string.cpp_wizard_welcome_title, R.string.cpp_wizard_start),
    CHOOSE_MODE(ChooseModeWizardStep::class.java, R.string.cpp_wizard_mode_title),
    CHOOSE_THEME(ChooseThemeWizardStep::class.java, R.string.cpp_wizard_theme_title),
    ON_SCREEN_CALCULATOR(OnScreenCalculatorWizardStep::class.java, R.string.cpp_wizard_onscreen_calculator_title),
    DRAG_BUTTON(DragButtonWizardStep::class.java, R.string.cpp_wizard_dragbutton_title),
    LAST(FinalWizardStep::class.java, R.string.cpp_wizard_final_title);

    override val fragmentTag: String
        get() = this.name

    override val fragmentArgs: Bundle?
        get() = null

    override fun onNext(fragment: Fragment): Boolean = true

    override fun onPrev(fragment: Fragment): Boolean = true

    override val isVisible: Boolean
        get() = true
}
