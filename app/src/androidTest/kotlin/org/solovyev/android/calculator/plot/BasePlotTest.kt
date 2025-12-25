package org.solovyev.android.calculator.plot

import android.test.suitebuilder.annotation.LargeTest
import android.text.TextUtils
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.runner.RunWith
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.functions.CppFunction
import org.solovyev.android.calculator.functions.FunctionParamsView

@RunWith(AndroidJUnit4::class)
@LargeTest
open class BasePlotTest {
    @Rule
    @JvmField
    var rule = ActivityTestRule(PlotActivity::class.java)

    protected fun addFunction(function: CppFunction) {
        openFunctionEditor()

        if (!TextUtils.isEmpty(function.name)) {
            onView(withId(R.id.function_name)).perform(typeText(function.name))
        }

        for (parameter in function.parameters) {
            onView(withId(R.id.function_params_add)).perform(click())
            onView(allOf(hasFocus(), withTagValue(Matchers.equalTo<Any>(FunctionParamsView.PARAM_VIEW_TAG)))).perform(click(), typeTextIntoFocusedView(parameter))
        }

        onView(withId(R.id.function_body)).perform(typeText(function.body))
        onView(withText(R.string.cpp_done)).perform(click())
    }

    protected fun openFunctionEditor() {
        onView(withId(R.id.plot_view_frame)).perform(ViewActions.click())
        onView(withId(R.id.plot_add_function)).perform(click())
    }

    protected fun openFunctionsList() {
        onView(withId(R.id.plot_view_frame)).perform(ViewActions.click())
        onView(withId(R.id.plot_functions)).perform(click())
    }
}
