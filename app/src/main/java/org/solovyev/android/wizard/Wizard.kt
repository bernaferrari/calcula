package org.solovyev.android.wizard

interface Wizard {
    val lastSavedStepName: String?
    val isFinished: Boolean
    val isStarted: Boolean
    val flow: WizardFlow
    val name: String

    fun saveLastStep(step: WizardStep)
    fun saveFinished(step: WizardStep, forceFinish: Boolean)
}
