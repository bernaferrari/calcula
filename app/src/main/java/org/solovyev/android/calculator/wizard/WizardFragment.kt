package org.solovyev.android.calculator.wizard

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.release.ChooseThemeReleaseNoteFragment
import org.solovyev.android.calculator.release.ChooseThemeReleaseNoteStep
import org.solovyev.android.calculator.release.ReleaseNoteFragment
import org.solovyev.android.calculator.release.ReleaseNoteStep
import org.solovyev.android.views.Adjuster
import org.solovyev.android.wizard.WizardStep
import javax.inject.Inject

@AndroidEntryPoint
abstract class WizardFragment : Fragment(), View.OnClickListener {

    protected var nextButton: TextView? = null
    protected var prevButton: TextView? = null

    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var typeface: Typeface

    private var step: WizardStep? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hilt @AndroidEntryPoint handles injection automatically
        step = findStepByClassName()
    }

    private fun findStepByClassName(): WizardStep {
        when (this) {
            is ReleaseNoteFragment -> return ReleaseNoteStep(arguments ?: Bundle())
            is ChooseThemeReleaseNoteFragment -> return ChooseThemeReleaseNoteStep(arguments ?: Bundle())
        }

        for (stepEnum in CalculatorWizardStep.values()) {
            if (stepEnum.fragmentClass == javaClass) {
                return stepEnum
            }
        }

        throw AssertionError("Wizard step for class $javaClass was not found")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_wizard, container, false)

        val content = view.findViewById<ViewGroup>(R.id.wizard_content)
        inflater.inflate(getViewResId(), content, true)
        Adjuster.maxWidth(content, resources.getDimensionPixelSize(R.dimen.cpp_wizard_max_width))

        nextButton = view.findViewById<TextView>(R.id.wizard_next)?.apply {
            setOnClickListener(this@WizardFragment)
        }

        prevButton = view.findViewById<TextView>(R.id.wizard_prev)?.apply {
            setOnClickListener(this@WizardFragment)
        }

        val wizard = wizardActivity.getWizard()
        val flow = wizardActivity.getFlow()
        val currentStep = getStep()
        val canGoNext = flow.getNextStep(currentStep) != null
        val canGoPrev = flow.getPrevStep(currentStep) != null
        val firstTimeWizard = TextUtils.equals(wizard.name, CalculatorWizards.FIRST_TIME_WIZARD)

        when {
            canGoNext -> {
                if (canGoPrev || !firstTimeWizard) {
                    setupNextButton(R.string.cpp_wizard_next)
                } else {
                    setupNextButton(R.string.cpp_wizard_start)
                }
            }
            else -> setupNextButton(R.string.cpp_wizard_finish)
        }

        when {
            canGoPrev -> setupPrevButton(R.string.cpp_wizard_back)
            firstTimeWizard -> setupPrevButton(R.string.cpp_wizard_skip)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BaseActivity.fixFonts(view, typeface)
    }

    protected fun setupNextButton(textResId: Int) {
        nextButton?.apply {
            setText(textResId)
            visibility = View.VISIBLE
        }
    }

    protected fun setupPrevButton(textResId: Int) {
        prevButton?.apply {
            setText(textResId)
            visibility = View.VISIBLE
        }
    }

    @LayoutRes
    protected abstract fun getViewResId(): Int

    override fun onClick(v: View) {
        val activity = wizardActivity
        when (v.id) {
            R.id.wizard_next -> {
                if (activity.canGoNext()) {
                    activity.goNext()
                } else {
                    activity.finishWizard()
                }
            }
            R.id.wizard_prev -> {
                if (activity.canGoPrev()) {
                    activity.goPrev()
                } else {
                    activity.finishWizardAbruptly()
                }
            }
        }
    }

    private val wizardActivity: WizardActivity
        get() = requireActivity() as WizardActivity

    fun getStep(): WizardStep {
        if (step == null) {
            step = findStepByClassName()
        }
        return step!!
    }

    fun onNext() {
        getStep().onNext(this)
    }

    fun onPrev() {
        getStep().onPrev(this)
    }
}
