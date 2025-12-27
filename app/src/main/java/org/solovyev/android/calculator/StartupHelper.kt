package org.solovyev.android.calculator

import android.os.Bundle
import android.app.Activity
import kotlinx.coroutines.runBlocking
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.release.ReleaseNotes.hasReleaseNotes
import org.solovyev.android.calculator.wizard.CalculatorWizards
import org.solovyev.android.wizard.Wizards
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartupHelper @Inject constructor(
    private val appPreferences: AppPreferences,
    private val wizards: Wizards
) {
    /**
     * Called when main activity is opened.
     * @return true if the rate us dialog should be shown
     */
    fun onMainActivityOpened(activity: Activity): Boolean {
        val opened = runBlocking { appPreferences.ui.incrementOpened() }
        // We no longer launch wizard activities here. It is handled by Navigation 3.

        // Update version code if needed
        val currentVersion = App.getAppVersionCode(activity)
        runBlocking { appPreferences.ui.setAppVersion(currentVersion) }

        return shouldShowRateUsDialog(opened)
    }

    fun getPendingWizard(activity: Activity): Pair<String, String?>? {
        val currentVersion = App.getAppVersionCode(activity)
        val wizard = wizards.getWizard(CalculatorWizards.FIRST_TIME_WIZARD)

        if (wizard.isStarted && !wizard.isFinished) {
            // Continuation
            return wizard.name to null // WizardUi logic would load last step automatically
        }

        if (appPreferences.ui.getAppVersionBlocking() == null) {
            // New start
            return wizard.name to null
        }

        val savedVersion = appPreferences.ui.getAppVersionBlocking()
        if (savedVersion != null && savedVersion < currentVersion) {
            if (appPreferences.settings.getShowReleaseNotesBlocking() &&
                hasReleaseNotes(activity, savedVersion + 1)
            ) {
                return CalculatorWizards.RELEASE_NOTES to null
            }
        }

        return null
    }

    fun markRateUsShown() {
        runBlocking { appPreferences.ui.setRateUsShown(true) }
    }

    private fun shouldShowRateUsDialog(opened: Int): Boolean {
        return opened > 30 && appPreferences.ui.getRateUsShownBlocking() != true
    }
}
