package org.solovyev.android.wizard

interface WizardFlow {
    fun getStepByName(name: String): WizardStep?
    fun getNextStep(step: WizardStep): WizardStep?
    fun getPrevStep(step: WizardStep): WizardStep?
    val firstStep: WizardStep
}
