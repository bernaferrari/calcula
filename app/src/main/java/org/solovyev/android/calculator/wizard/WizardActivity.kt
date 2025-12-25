package org.solovyev.android.calculator.wizard

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.viewpagerindicator.PageIndicator
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.language.Languages
import org.solovyev.android.wizard.ListWizardFlow
import org.solovyev.android.wizard.Wizard
import org.solovyev.android.wizard.WizardFlow
import org.solovyev.android.wizard.WizardUi
import org.solovyev.android.wizard.Wizards
import org.solovyev.android.wizard.WizardsAware
import javax.inject.Inject

@AndroidEntryPoint
class WizardActivity : BaseActivity(R.layout.cpp_activity_wizard, 0),
    WizardsAware, SharedPreferences.OnSharedPreferenceChangeListener {

    private val wizardUi: WizardUi<WizardActivity> = WizardUi(this, this, 0)
    private val dialogListener = DialogListener()

    private lateinit var pager: ViewPager
    private lateinit var pagerAdapter: WizardPagerAdapter
    private var dialog: AlertDialog? = null

    @Inject
    override lateinit var wizards: Wizards

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wizardUi.onCreate(savedInstanceState)

        val flow = wizardUi.flow as ListWizardFlow

        pager = findViewById(R.id.pager)
        pagerAdapter = WizardPagerAdapter(flow, supportFragmentManager)
        pager.adapter = pagerAdapter

        val titleIndicator: PageIndicator = findViewById(R.id.pager_indicator)
        titleIndicator.setViewPager(pager)

        val wizard = wizardUi.getWizard()
        titleIndicator.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                val step = flow.getStepAt(position)
                wizardUi.step = step
                WizardUi.tryPutStep(intent, step)
                wizard.saveLastStep(step)
            }
        })

        if (savedInstanceState == null) {
            wizardUi.step?.let { currentStep ->
                val position = flow.getPositionFor(currentStep)
                pager.currentItem = position
            }
        }

        wizardUi.step?.let { currentStep ->
            if (wizard.lastSavedStepName == null) {
                wizard.saveLastStep(currentStep)
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onBackPressed() {
        if (pager.currentItem == 0) {
            finishWizardAbruptly()
        } else {
            pager.currentItem = pager.currentItem - 1
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        wizardUi.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        wizardUi.onPause()
    }

    fun finishWizardAbruptly() {
        val wizardName = wizardUi.getWizard().name
        val confirmed = wizardName == CalculatorWizards.RELEASE_NOTES ||
                wizardName == CalculatorWizards.DEFAULT_WIZARD_FLOW
        finishWizardAbruptly(confirmed)
    }

    fun finishWizardAbruptly(confirmed: Boolean) {
        if (!confirmed) {
            if (dialog != null) {
                return
            }

            val builder = AlertDialog.Builder(this, App.getTheme().alertDialogTheme)
            builder.setTitle(R.string.cpp_wizard_finish_confirmation_title)
                .setMessage(R.string.cpp_wizard_finish_confirmation)
                .setNegativeButton(R.string.cpp_no, dialogListener)
                .setPositiveButton(R.string.cpp_yes, dialogListener)
                .setOnCancelListener(dialogListener)

            dialog = builder.create()
            dialog?.setOnDismissListener(dialogListener)
            dialog?.show()
            return
        }

        dismissDialog()
        wizardUi.finishWizardAbruptly()
        finish()
    }

    fun finishWizard() {
        wizardUi.finishWizard()
        finish()
    }

    fun canGoNext(): Boolean {
        val position = pager.currentItem
        return position != pagerAdapter.count - 1
    }

    fun canGoPrev(): Boolean {
        val position = pager.currentItem
        return position != 0
    }

    fun goNext() {
        val position = pager.currentItem
        if (position < pagerAdapter.count - 1) {
            val fragment = pagerAdapter.getItem(position) as WizardFragment
            fragment.onNext()
            pager.setCurrentItem(position + 1, true)
        }
    }

    fun goPrev() {
        val position = pager.currentItem
        if (position > 0) {
            val fragment = pagerAdapter.getItem(position) as WizardFragment
            fragment.onPrev()
            pager.setCurrentItem(position - 1, true)
        }
    }

    fun getFlow(): WizardFlow = wizardUi.flow

    fun getWizard(): Wizard = wizardUi.getWizard()

    override fun onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(this)
        dismissDialog()
        super.onDestroy()
    }

    private fun dismissDialog() {
        dialog?.dismiss()
        dialog = null
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Handle preference changes if needed
    }

    private inner class WizardPagerAdapter(
        private val flow: ListWizardFlow,
        fm: FragmentManager
    ) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            val step = flow.getStepAt(position)
            val className = step.fragmentClass.name
            val args = step.fragmentArgs
            return Fragment.instantiate(this@WizardActivity, className, args)
        }

        override fun getCount(): Int = flow.count
    }

    private inner class DialogListener : DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener, DialogInterface.OnCancelListener {

        override fun onClick(dialog: DialogInterface?, which: Int) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                finishWizardAbruptly(true)
            }
        }

        override fun onDismiss(d: DialogInterface?) {
            dialog = null
        }

        override fun onCancel(d: DialogInterface?) {
            dialog = null
        }
    }
}
