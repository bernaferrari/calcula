package org.solovyev.android.calculator.wizard

import android.os.Bundle
import android.view.View
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.R

@AndroidEntryPoint
class FinalWizardStep : WizardFragment() {

    override fun getViewResId(): Int = R.layout.cpp_wizard_step_final

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (App.getTheme().light) {
            val message = view.findViewById<TextView>(R.id.wizard_final_message)
            message.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_action_done_light, 0, 0)
        }
    }
}
