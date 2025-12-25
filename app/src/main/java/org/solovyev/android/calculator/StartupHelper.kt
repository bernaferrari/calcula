package org.solovyev.android.calculator

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import org.solovyev.android.calculator.release.ReleaseNotes.hasReleaseNotes
import org.solovyev.android.calculator.wizard.CalculatorWizards
import org.solovyev.android.wizard.WizardUi
import org.solovyev.android.wizard.Wizards
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartupHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: SharedPreferences,
    private val wizards: Wizards
) {
    // UI preferences accessed via context
    private val uiPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("ui", Context.MODE_PRIVATE)
    }

    fun onMainActivityOpened(activity: AppCompatActivity) {
        val editor = uiPreferences.edit()
        val opened = UiPreferences.opened.getPreference(uiPreferences)
        UiPreferences.opened.putPreference(editor, (opened ?: 0) + 1)
        handleOnMainActivityOpened(activity, editor, opened ?: 0)
        UiPreferences.appVersion.putPreference(editor, App.getAppVersionCode(activity))
        editor.apply()
    }

    private fun handleOnMainActivityOpened(
        activity: AppCompatActivity,
        editor: SharedPreferences.Editor,
        opened: Int
    ) {
        val currentVersion = App.getAppVersionCode(activity)
        val wizard = wizards.getWizard(CalculatorWizards.FIRST_TIME_WIZARD)

        if (wizard.isStarted && !wizard.isFinished) {
            WizardUi.continueWizard(wizards, wizard.name, activity)
            return
        }

        if (!UiPreferences.appVersion.isSet(uiPreferences)) {
            // new start
            WizardUi.startWizard(wizards, activity)
            return
        }

        val savedVersion = UiPreferences.appVersion.getPreference(uiPreferences)
        if (savedVersion != null && savedVersion < currentVersion) {
            if (Preferences.Gui.showReleaseNotes.getPreference(preferences) == true &&
                hasReleaseNotes(activity, savedVersion + 1)) {
                val bundle = Bundle().apply {
                    putInt(CalculatorWizards.RELEASE_NOTES_VERSION, savedVersion)
                }
                activity.startActivity(
                    WizardUi.createLaunchIntent(wizards, CalculatorWizards.RELEASE_NOTES, activity, bundle)
                )
                return
            }
        }

        if (shouldShowRateUsDialog(opened)) {
            AlertDialog.Builder(activity, App.getTheme().alertDialogTheme)
                .setPositiveButton(R.string.cpp_rateus_ok) { _, _ ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://market.android.com/details?id=org.solovyev.android.calculator")
                        }
                        activity.startActivity(intent)
                    } catch (ignored: ActivityNotFoundException) {
                    }
                }
                .setNegativeButton(R.string.cpp_rateus_cancel, null)
                .setMessage(
                    activity.getString(
                        R.string.cpp_rateus_message,
                        activity.getString(R.string.cpp_app_name)
                    )
                )
                .setTitle(
                    activity.getString(
                        R.string.cpp_rateus_title,
                        activity.getString(R.string.cpp_app_name)
                    )
                )
                .create()
                .show()
            UiPreferences.rateUsShown.putPreference(editor, true)
        }
    }

    private fun shouldShowRateUsDialog(opened: Int): Boolean {
        return opened > 30 && UiPreferences.rateUsShown.getPreference(uiPreferences) != true
    }
}
