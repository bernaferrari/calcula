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
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.R

@AndroidEntryPoint
class OnScreenCalculatorWizardStep : WizardFragment(), CompoundButton.OnCheckedChangeListener {

    private var checkbox: CheckBox? = null

    override fun getViewResId(): Int = R.layout.cpp_wizard_step_onscreen

    override fun onViewCreated(root: View, savedInstanceState: Bundle?) {
        super.onViewCreated(root, savedInstanceState)

        val enabled = Preferences.Onscreen.showAppIcon.getPreference(preferences) ?: true
        checkbox = root.findViewById<CheckBox>(R.id.wizard_onscreen_app_enabled_checkbox).apply {
            isChecked = enabled
            setOnCheckedChangeListener(this@OnScreenCalculatorWizardStep)
        }

        if (App.getTheme().light) {
            val message = root.findViewById<TextView>(R.id.wizard_onscreen_message)
            message.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.logo_wizard_window_light, 0, 0)
        }
    }

    fun getCheckbox(): CheckBox? = checkbox

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        Preferences.Onscreen.showAppIcon.putPreference(preferences, isChecked)
    }
}
