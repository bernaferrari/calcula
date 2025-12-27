package org.solovyev.android.calculator

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import dagger.Lazy
import jscl.math.Generic
import jscl.math.function.CustomFunction
import org.solovyev.android.Check
import org.solovyev.android.calculator.about.AboutActivity
import org.solovyev.android.calculator.functions.CppFunction
import org.solovyev.android.calculator.functions.FunctionsActivity
import org.solovyev.android.calculator.history.HistoryActivity
import org.solovyev.android.calculator.operators.OperatorsActivity
import org.solovyev.android.calculator.plot.ExpressionFunction
import org.solovyev.android.calculator.plot.PlotActivity
import org.solovyev.android.calculator.preferences.PreferencesActivity
import org.solovyev.android.calculator.preferences.SettingsDestination
import org.solovyev.android.calculator.variables.CppVariable
import org.solovyev.android.calculator.variables.VariablesActivity
import org.solovyev.android.plotter.PlotFunction
import org.solovyev.android.plotter.Plotter
import org.solovyev.common.msg.MessageType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityLauncher @Inject constructor() {

    @Inject
    lateinit var application: Application

    @Inject
    lateinit var plotter: Lazy<Plotter>

    @Inject
    lateinit var errorReporter: Lazy<ErrorReporter>

    @Inject
    lateinit var display: Lazy<Display>

    @Inject
    lateinit var variablesRegistry: Lazy<VariablesRegistry>

    @Inject
    lateinit var notifier: Notifier

    private var activity: CalculatorActivity? = null

    fun plotDisplayedExpression() {
        val state = display.get().getState()
        if (!state.valid) {
            notifier.showMessage(R.string.not_valid_result, MessageType.error)
            return
        }
        plot(state.getResult())
    }

    fun showHistory() {
        show(context, HistoryActivity.getClass(context))
    }

    fun showSettings() {
        show(context, PreferencesActivity.getClass(context))
    }

    fun showWidgetSettings() {
        show(
            context,
            PreferencesActivity.makeIntent(context, SettingsDestination.WIDGET)
        )
    }

    fun showOperators() {
        show(context, OperatorsActivity.getClass(context))
    }

    fun showAbout() {
        show(context, AboutActivity.getClass(context))
    }

    fun showPlotter() {
        show(context, PlotActivity::class.java)
    }

    fun openFacebook() {
        val uri = Uri.parse(application.getString(R.string.cpp_share_link))
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        application.startActivity(intent)
    }

    fun setActivity(activity: CalculatorActivity?) {
        Check.isNull(this.activity)
        this.activity = activity
    }

    fun clearActivity(activity: CalculatorActivity?) {
        Check.isNotNull(this.activity)
        Check.equals(this.activity, activity)
        this.activity = null
    }

    fun show(activity: Class<HistoryActivity>) {
        show(context, activity)
    }

    private val context: Context
        get() = activity ?: application

    fun showFunctions() {
        show(context, FunctionsActivity.getClass(context))
    }

    fun showVariables() {
        show(context, VariablesActivity.getClass(context))
    }

    fun openApp() {
        val intent = Intent(context, CalculatorActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }

    internal fun plot(expression: Generic?) {
        if (expression == null) {
            notifier.showMessage(R.string.cpp_plot_empty_function_error)
            return
        }

        val content = expression.toString()
        if (TextUtils.isEmpty(content)) {
            notifier.showMessage(R.string.cpp_plot_empty_function_error)
            return
        }

        val parameters = expression.getUndefinedConstants(variablesRegistry.get())
            .map { it.name }

        if (parameters.size > 2) {
            notifier.showMessage(R.string.cpp_plot_too_many_variables)
            return
        }

        try {
            val f = CustomFunction.Builder()
                .setName("")
                .setParameterNames(parameters)
                .setContent(content)
                .create()

            val ef = ExpressionFunction(f)
            val color = org.solovyev.android.plotter.Color.create(0xFF0099CC.toInt())
            val width = org.solovyev.android.plotter.meshes.MeshSpec.defaultWidth(context)
            val meshSpec = org.solovyev.android.plotter.meshes.MeshSpec(color, width).apply {
                pointsCount = PlotActivity.POINTS_COUNT
            }
            val pf = PlotFunction.create(ef, meshSpec)

            plotter.get().add(pf)
            showPlotter()
        } catch (e: RuntimeException) {
            errorReporter.get().onException(e)
            notifier.showMessage(e)
        }
    }

    fun canPlot(expression: Generic?): Boolean {
        if (expression == null || TextUtils.isEmpty(expression.toString())) {
            return false
        }
        if (expression.getUndefinedConstants(variablesRegistry.get()).size > 2) {
            return false
        }
        return true
    }

    fun showConstantEditor() {
        val state = display.get().getState()
        if (!state.valid) {
            notifier.showMessage(R.string.not_valid_result)
            return
        }
        val variable = CppVariable.builder("").withValue(state.text).build()
        val intent = Intent(context, VariablesActivity.getClass(context)).apply {
            putExtra(VariablesActivity.EXTRA_VARIABLE, variable)
            App.addIntentFlags(this, context !is Activity, context)
        }
        context.startActivity(intent)
    }

    fun showFunctionEditor() {
        val state = display.get().getState()
        if (!state.valid) {
            notifier.showMessage(R.string.not_valid_result)
            return
        }

        val builder = CppFunction.builder("", state.text)
        state.getResult()?.let { expression ->
            expression.getUndefinedConstants(variablesRegistry.get()).forEach { constant ->
                builder.withParameter(constant.name)
            }
        }

        val intent = Intent(context, FunctionsActivity.getClass(context)).apply {
            putExtra(FunctionsActivity.EXTRA_FUNCTION, builder.build())
            App.addIntentFlags(this, context !is Activity, context)
        }
        context.startActivity(intent)
    }

    private companion object {
        private fun show(context: Context, activityClass: Class<out Activity>) {
            show(context, Intent(context, activityClass))
        }

        private fun show(context: Context, intent: Intent) {
            val detached = context !is Activity
            App.addIntentFlags(intent, detached, context)
            context.startActivity(intent)
        }
    }
}
