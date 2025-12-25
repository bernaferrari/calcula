package org.solovyev.android.calculator.release

import android.os.Bundle
import android.view.View
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.wizard.ChooseThemeWizardStep

@AndroidEntryPoint
class ChooseThemeReleaseNoteFragment : ChooseThemeWizardStep() {

    override fun onViewCreated(root: View, savedInstanceState: Bundle?) {
        super.onViewCreated(root, savedInstanceState)
        root.findViewById<TextView>(R.id.wizard_theme_title).apply {
            setText(R.string.cpp_release_notes_choose_theme)
        }
    }
}
