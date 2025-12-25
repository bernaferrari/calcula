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
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.keyboard.BaseKeyboardUi
import org.solovyev.android.views.Adjuster
import org.solovyev.android.views.dragbutton.DirectionDragButton
import org.solovyev.android.views.dragbutton.DragDirection

@AndroidEntryPoint
class ChooseModeWizardStep : WizardFragment(), AdapterView.OnItemSelectedListener {

    private lateinit var button: DirectionDragButton
    private lateinit var description: TextView

    override fun getViewResId(): Int = R.layout.cpp_wizard_step_choose_mode

    override fun onViewCreated(root: View, savedInstanceState: Bundle?) {
        super.onViewCreated(root, savedInstanceState)

        val mode = CalculatorMode.fromGuiLayout(Preferences.Gui.mode.getPreference(preferences) ?: Preferences.Gui.Mode.simple)
        val spinner = root.findViewById<Spinner>(R.id.wizard_mode_spinner)
        spinner.adapter = WizardArrayAdapter.create(requireActivity(), R.array.cpp_modes)
        spinner.setSelection(if (mode == CalculatorMode.simple) 0 else 1)
        spinner.onItemSelectedListener = this

        button = root.findViewById(R.id.wizard_mode_button)
        Adjuster.adjustText(button, BaseKeyboardUi.getTextScale(requireActivity()))
        description = root.findViewById(R.id.wizard_mode_description)
        updateDescription(mode)
    }

    private fun updateDescription(mode: CalculatorMode) {
        val simple = mode == CalculatorMode.simple
        description.setText(
            if (simple) R.string.cpp_wizard_mode_simple_description
            else R.string.cpp_wizard_mode_engineer_description
        )

        if (simple) {
            button.setText(DragDirection.up, "")
            button.setText(DragDirection.down, "")
            button.setText(DragDirection.left, "")
        } else {
            button.setText(DragDirection.up, "sin")
            button.setText(DragDirection.down, "ln")
            button.setText(DragDirection.left, "i")
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val mode = if (position == 0) CalculatorMode.simple else CalculatorMode.engineer
        mode.apply(preferences)
        updateDescription(mode)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // No action needed
    }
}
