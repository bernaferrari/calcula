package org.solovyev.android.wizard

import android.content.Context
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.di.AppEntryPoint
import org.solovyev.android.calculator.di.AppPreferences

class BaseWizard(
    override val name: String,
    private val context: Context,
    override val flow: WizardFlow
) : Wizard {

    private val appPreferences: AppPreferences by lazy {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppEntryPoint::class.java
        ).appPreferences()
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val lastSavedStepName: String?
        get() = appPreferences.wizard.getLastStepBlocking(name)

    override val isFinished: Boolean
        get() = appPreferences.wizard.getFinishedBlocking(name)

    override val isStarted: Boolean
        get() = lastSavedStepName != null

    override fun saveLastStep(step: WizardStep) {
        scope.launch {
            appPreferences.wizard.setLastStep(name, step.name)
        }
    }

    override fun saveFinished(step: WizardStep, forceFinish: Boolean) {
        scope.launch {
            appPreferences.wizard.setFinished(
                name,
                forceFinish || flow.getNextStep(step) == null
            )
        }
    }
}
