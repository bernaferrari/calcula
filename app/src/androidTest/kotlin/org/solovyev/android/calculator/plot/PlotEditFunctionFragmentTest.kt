package org.solovyev.android.calculator.plot

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Test
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.functions.CppFunction
import org.solovyev.android.calculator.functions.FunctionParamsView

class PlotEditFunctionFragmentTest : BasePlotTest() {

    @Test
    fun testShouldAddFunction() {
        val function = CppFunction.builder("", "x + y").withParameters("x", "y").build()
        addFunction(function)

        openFunctionsList()
        onView(withId(R.id.function_name)).check(matches(withText("x+y")))
    }

    @Test
    fun testShouldHaveOnlyTwoParameters() {
        openFunctionEditor()

        onView(withId(R.id.function_params_add)).check(matches(isDisplayed()))

        onView(withId(R.id.function_params_add)).perform(click())
        onView(withId(R.id.function_params_add)).check(matches(isDisplayed()))
        onView(withId(R.id.function_params_add)).perform(click())

        onView(withId(R.id.function_params_add)).check(matches(not(isDisplayed())))
    }

    @Test
    fun testShouldProvideDefaultParamNames() {
        openFunctionEditor()

        onView(withId(R.id.function_params_add)).perform(click())
        onView(allOf(hasFocus(), withTagValue(Matchers.equalTo<Any>(FunctionParamsView.PARAM_VIEW_TAG)))).check(matches(withText("x")))
    }

    @Test
    fun testShouldSelectParamOnFocus() {
        openFunctionEditor()

        onView(withId(R.id.function_params_add)).perform(click())
        // check "select-on-focus" attribute
        onView(allOf(hasFocus(), withTagValue(Matchers.equalTo<Any>(FunctionParamsView.PARAM_VIEW_TAG)))).perform(typeTextIntoFocusedView("y"))
        onView(allOf(hasFocus(), withTagValue(Matchers.equalTo<Any>(FunctionParamsView.PARAM_VIEW_TAG)))).check(matches(withText("y")))
    }
}
