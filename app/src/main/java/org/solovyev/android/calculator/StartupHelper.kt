package org.solovyev.android.calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.release.ReleaseNotes.hasReleaseNotes
import org.solovyev.android.calculator.wizard.CalculatorWizards
import org.solovyev.android.wizard.WizardUi
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
    fun onMainActivityOpened(activity: AppCompatActivity): Boolean {
        val opened = runBlocking { appPreferences.ui.incrementOpened() }
        return handleOnMainActivityOpened(activity, opened)
    }

    private fun handleOnMainActivityOpened(
        activity: AppCompatActivity,
        opened: Int
    ): Boolean {
        val currentVersion = App.getAppVersionCode(activity)
        val wizard = wizards.getWizard(CalculatorWizards.FIRST_TIME_WIZARD)

        if (wizard.isStarted && !wizard.isFinished) {
            WizardUi.continueWizard(wizards, wizard.name, activity)
            runBlocking { appPreferences.ui.setAppVersion(currentVersion) }
            return false
        }

        if (appPreferences.ui.getAppVersionBlocking() == null) {
            // new start
            WizardUi.startWizard(wizards, activity)
            runBlocking { appPreferences.ui.setAppVersion(currentVersion) }
            return false
        }

        val savedVersion = appPreferences.ui.getAppVersionBlocking()
        if (savedVersion != null && savedVersion < currentVersion) {
            if (appPreferences.settings.getShowReleaseNotesBlocking() &&
                hasReleaseNotes(activity, savedVersion + 1)) {
                val bundle = Bundle().apply {
                    putInt(CalculatorWizards.RELEASE_NOTES_VERSION, savedVersion)
                }
                activity.startActivity(
                    WizardUi.createLaunchIntent(wizards, CalculatorWizards.RELEASE_NOTES, activity, bundle)
                )
                runBlocking { appPreferences.ui.setAppVersion(currentVersion) }
                return false
            }
        }

        runBlocking { appPreferences.ui.setAppVersion(currentVersion) }
        return shouldShowRateUsDialog(opened)
    }

    fun markRateUsShown() {
        runBlocking { appPreferences.ui.setRateUsShown(true) }
    }

    private fun shouldShowRateUsDialog(opened: Int): Boolean {
        return opened > 30 && appPreferences.ui.getRateUsShownBlocking() != true
    }
}
