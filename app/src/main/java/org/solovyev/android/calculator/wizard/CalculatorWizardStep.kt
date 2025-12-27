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

    WELCOME(WizardPlaceholderFragment::class.java, R.string.cpp_wizard_welcome_title, R.string.cpp_wizard_start),
    CHOOSE_MODE(WizardPlaceholderFragment::class.java, R.string.cpp_wizard_mode_title),
    CHOOSE_THEME(WizardPlaceholderFragment::class.java, R.string.cpp_wizard_theme_title),
    ON_SCREEN_CALCULATOR(WizardPlaceholderFragment::class.java, R.string.cpp_wizard_onscreen_calculator_title),
    DRAG_BUTTON(WizardPlaceholderFragment::class.java, R.string.cpp_wizard_dragbutton_title),
    LAST(WizardPlaceholderFragment::class.java, R.string.cpp_wizard_final_title);

    override val fragmentTag: String
        get() = this.name

    override val fragmentArgs: Bundle?
        get() = null

    override fun onNext(fragment: Fragment): Boolean = true

    override fun onPrev(fragment: Fragment): Boolean = true

    override val isVisible: Boolean
        get() = true
}
