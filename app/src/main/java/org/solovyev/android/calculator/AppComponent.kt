package org.solovyev.android.calculator

import dagger.Component
import org.solovyev.android.calculator.converter.ConverterFragment
import org.solovyev.android.calculator.errors.FixableErrorFragment
import org.solovyev.android.calculator.errors.FixableErrorsActivity
import org.solovyev.android.calculator.floating.FloatingCalculatorBroadcastReceiver
import org.solovyev.android.calculator.floating.FloatingCalculatorService
import org.solovyev.android.calculator.floating.FloatingCalculatorView
import org.solovyev.android.calculator.functions.BaseFunctionFragment
import org.solovyev.android.calculator.functions.FunctionsFragment
import org.solovyev.android.calculator.history.BaseHistoryFragment
import org.solovyev.android.calculator.history.EditHistoryFragment
import org.solovyev.android.calculator.history.HistoryActivity
import org.solovyev.android.calculator.keyboard.BaseKeyboardUi
import org.solovyev.android.calculator.operators.OperatorsFragment
import org.solovyev.android.calculator.plot.PlotActivity
import org.solovyev.android.calculator.plot.PlotDimensionsFragment
import org.solovyev.android.calculator.plot.PlotEditFunctionFragment
import org.solovyev.android.calculator.plot.PlotFunctionsFragment
import org.solovyev.android.calculator.preferences.PreferencesActivity
import org.solovyev.android.calculator.preferences.PreferencesFragment
import org.solovyev.android.calculator.preferences.PurchaseDialogActivity
import org.solovyev.android.calculator.variables.EditVariableFragment
import org.solovyev.android.calculator.variables.VariablesFragment
import org.solovyev.android.calculator.view.Tabs
import org.solovyev.android.calculator.widget.CalculatorWidget
import org.solovyev.android.calculator.wizard.DragButtonWizardStep
import org.solovyev.android.calculator.wizard.WizardActivity
import org.solovyev.android.calculator.wizard.WizardFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, LegacyModule::class, CoreModule::class])
interface AppComponent {
    fun inject(application: CalculatorApplication)
    fun inject(fragment: EditorFragment)
    fun inject(service: FloatingCalculatorService)
    fun inject(fragment: BaseHistoryFragment)
    fun inject(fragment: BaseDialogFragment)
    fun inject(fragment: FixableErrorFragment)
    fun inject(fragment: PlotEditFunctionFragment)
    fun inject(fragment: BaseFunctionFragment)
    fun inject(fragment: EditVariableFragment)
    fun inject(fragment: EditHistoryFragment)
    fun inject(fragment: PlotFunctionsFragment)
    fun inject(fragment: FunctionsFragment)
    fun inject(fragment: VariablesFragment)
    fun inject(fragment: OperatorsFragment)
    fun inject(fragment: ConverterFragment)
    fun inject(activity: CalculatorActivity)
    fun inject(activity: FixableErrorsActivity)
    fun inject(receiver: WidgetReceiver)
    fun inject(fragment: DisplayFragment)
    fun inject(fragment: KeyboardFragment)
    fun inject(activity: PurchaseDialogActivity)
    fun inject(activity: PreferencesActivity)
    fun inject(ui: BaseKeyboardUi)
    fun inject(view: FloatingCalculatorView)
    fun inject(fragment: DragButtonWizardStep)
    fun inject(fragment: BaseFragment)
    fun inject(activity: HistoryActivity)
    fun inject(tabs: Tabs)
    fun inject(widget: CalculatorWidget)
    fun inject(activity: WizardActivity)
    fun inject(activity: BaseActivity)
    fun inject(fragment: PreferencesFragment)
    fun inject(fragment: WizardFragment)
    fun inject(receiver: FloatingCalculatorBroadcastReceiver)
}
