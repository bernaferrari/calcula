package org.solovyev.android.calculator.wizard

import android.app.Activity
import android.content.Context
import android.os.Bundle
import org.solovyev.android.calculator.release.ChooseThemeReleaseNoteStep
import org.solovyev.android.calculator.release.ReleaseNoteStep
import org.solovyev.android.calculator.release.ReleaseNotes
import org.solovyev.android.wizard.BaseWizard
import org.solovyev.android.wizard.ListWizardFlow
import org.solovyev.android.wizard.Wizard
import org.solovyev.android.wizard.WizardFlow
import org.solovyev.android.wizard.WizardStep
import org.solovyev.android.wizard.Wizards

class CalculatorWizards(private val context: Context) : Wizards {

    override val activityClassName: Class<out Activity> = WizardActivity::class.java

    override fun getWizard(name: String?, arguments: Bundle?): Wizard {
        val wizardName = name ?: FIRST_TIME_WIZARD

        return when (wizardName) {
            FIRST_TIME_WIZARD -> newBaseWizard(FIRST_TIME_WIZARD, newFirstTimeWizardFlow())
            DEFAULT_WIZARD_FLOW -> newBaseWizard(DEFAULT_WIZARD_FLOW, newDefaultWizardFlow())
            RELEASE_NOTES -> newBaseWizard(RELEASE_NOTES, newReleaseNotesWizardFlow(context, arguments))
            else -> throw IllegalArgumentException("Wizard flow $wizardName is not supported")
        }
    }

    private fun newBaseWizard(name: String, flow: WizardFlow): BaseWizard {
        return BaseWizard(name, context, flow)
    }

    companion object {
        const val FIRST_TIME_WIZARD = "first-wizard"
        const val RELEASE_NOTES = "release-notes"
        const val RELEASE_NOTES_VERSION = "version"
        const val DEFAULT_WIZARD_FLOW = "app-wizard"

        fun newDefaultWizardFlow(): WizardFlow {
            val wizardSteps = CalculatorWizardStep.values()
                .filter { it != CalculatorWizardStep.WELCOME && it != CalculatorWizardStep.LAST && it.isVisible }
            return ListWizardFlow(wizardSteps)
        }

        fun newReleaseNotesWizardFlow(context: Context, arguments: Bundle?): WizardFlow {
            val startVersion = arguments?.getInt(RELEASE_NOTES_VERSION, 0) ?: 0
            var versions = ReleaseNotes.getReleaseNotesVersions(context, startVersion)

            if (versions.size > 7) {
                versions = versions.subList(0, 7)
            }

            val wizardSteps = versions.map { version ->
                when (version) {
                    ChooseThemeReleaseNoteStep.VERSION_CODE -> ChooseThemeReleaseNoteStep(version)
                    else -> ReleaseNoteStep(version)
                }
            }

            return ListWizardFlow(wizardSteps)
        }

        fun newFirstTimeWizardFlow(): WizardFlow {
            val wizardSteps = CalculatorWizardStep.values().filter { it.isVisible }
            return ListWizardFlow(wizardSteps)
        }
    }
}
