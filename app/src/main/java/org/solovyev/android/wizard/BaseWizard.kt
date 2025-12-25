package org.solovyev.android.wizard

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class BaseWizard(
    override val name: String,
    private val context: Context,
    override val flow: WizardFlow
) : Wizard {

    private val preferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(context)

    override val lastSavedStepName: String?
        get() = preferences.getString(makeLastStepPreferenceKey(name), null)

    override val isFinished: Boolean
        get() = preferences.getBoolean(makeFinishedPreferenceKey(name), false)

    override val isStarted: Boolean
        get() = lastSavedStepName != null

    override fun saveLastStep(step: WizardStep) {
        preferences.edit()
            .putString(makeLastStepPreferenceKey(name), step.name)
            .apply()
    }

    override fun saveFinished(step: WizardStep, forceFinish: Boolean) {
        preferences.edit()
            .putBoolean(makeFinishedPreferenceKey(name), forceFinish || flow.getNextStep(step) == null)
            .apply()
    }

    companion object {
        private const val FLOW = "flow"
        private const val FLOW_FINISHED = "flow_finished"

        private fun makeFinishedPreferenceKey(flowName: String): String =
            "$FLOW_FINISHED:$flowName"

        private fun makeLastStepPreferenceKey(flowName: String): String =
            "$FLOW:$flowName"
    }
}
