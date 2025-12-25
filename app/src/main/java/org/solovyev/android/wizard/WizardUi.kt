package org.solovyev.android.wizard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class WizardUi<A : FragmentActivity>(
    private val activity: A,
    private val wizardsAware: WizardsAware,
    private val layoutResId: Int
) {
    var step: WizardStep? = null
    private var wizard: Wizard? = null

    val flow: WizardFlow
        get() = wizard?.flow ?: throw IllegalStateException("Wizard not initialized")

    fun onCreate(savedInstanceState: Bundle?) {
        if (layoutResId != 0) {
            activity.setContentView(layoutResId)
        }

        val intent = activity.intent
        var wizardName = intent.getStringExtra(FLOW)
        var stepName = intent.getStringExtra(STEP)

        if (savedInstanceState != null) {
            wizardName = savedInstanceState.getString(FLOW)
            stepName = savedInstanceState.getString(STEP)
        }

        val arguments = intent.getBundleExtra(ARGUMENTS)
        wizard = wizardsAware.wizards.getWizard(wizardName, arguments)

        step = stepName?.let { wizard?.flow?.getStepByName(it) } ?: wizard?.flow?.firstStep
    }

    fun finishWizardAbruptly() {
        finishWizard(true)
    }

    fun finishWizard() {
        finishWizard(false)
    }

    private fun finishWizard(forceFinish: Boolean) {
        wizard?.let { w ->
            step?.let { s ->
                w.saveFinished(s, forceFinish)
            }
        }
        activity.finish()
    }

    fun onSaveInstanceState(out: Bundle) {
        wizard?.let { out.putString(FLOW, it.name) }
        step?.let { out.putString(STEP, it.name) }
    }

    fun onPause() {
        wizard?.let { w ->
            step?.let { s ->
                w.saveLastStep(s)
            }
        }
    }

    fun getWizard(): Wizard = wizard ?: throw IllegalStateException("Wizard not initialized")

    companion object {
        private const val FLOW = "flow"
        private const val ARGUMENTS = "arguments"
        private const val STEP = "step"

        @JvmStatic
        fun startWizard(wizards: Wizards, context: Context) {
            context.startActivity(createLaunchIntent(wizards, null, context))
        }

        @JvmStatic
        fun startWizard(wizards: Wizards, name: String?, context: Context) {
            context.startActivity(createLaunchIntent(wizards, name, context))
        }

        @JvmStatic
        fun continueWizard(wizards: Wizards, name: String, context: Context) {
            val intent = createLaunchIntent(wizards, name, context)
            val wizard = wizards.getWizard(name)
            val step = wizard.lastSavedStepName
            tryPutStep(intent, step)
            context.startActivity(intent)
        }

        @JvmStatic
        fun tryPutStep(intent: Intent, step: WizardStep?) {
            tryPutStep(intent, step?.name)
        }

        private fun tryPutStep(intent: Intent, step: String?) {
            step?.let { intent.putExtra(STEP, it) }
        }

        @JvmStatic
        fun createLaunchIntent(
            wizards: Wizards,
            name: String?,
            context: Context,
            arguments: Bundle? = null
        ): Intent {
            return Intent(context, wizards.activityClassName).apply {
                putExtra(FLOW, name)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                arguments?.let { putExtra(ARGUMENTS, it) }
            }
        }
    }
}
