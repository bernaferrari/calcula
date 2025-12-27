package org.solovyev.android.wizard

class ListWizardFlow(private val steps: List<WizardStep>) : WizardFlow {

    override fun getStepByName(name: String): WizardStep? =
        steps.find { it.name == name }

    override fun getNextStep(step: WizardStep): WizardStep? {
        val index = steps.indexOf(step)
        return if (index >= 0 && index + 1 < steps.size) {
            steps[index + 1]
        } else {
            null
        }
    }

    override fun getPrevStep(step: WizardStep): WizardStep? {
        val index = steps.indexOf(step)
        return if (index >= 1) {
            steps[index - 1]
        } else {
            null
        }
    }

    override val firstStep: WizardStep
        get() = steps[0]

    fun getStepAt(position: Int): WizardStep = steps[position]

    fun getPositionFor(step: WizardStep): Int =
        steps.indexOfFirst { it == step }

    val count: Int
        get() = steps.size
}
