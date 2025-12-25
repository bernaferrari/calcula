package org.solovyev.android.calculator.converter

import android.test.suitebuilder.annotation.LargeTest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.solovyev.android.calculator.CalculatorActivity
import org.solovyev.android.calculator.R

@RunWith(AndroidJUnit4::class)
@LargeTest
class ConverterFragmentTest {

    @Rule
    @JvmField
    var rule = ActivityTestRule(CalculatorActivity::class.java)

    @Test
    fun openConversionDialog() {
        openActionBarOverflowOrOptionsMenu(rule.activity)
        onView(withText(R.string.c_conversion_tool)).perform(click())
        onView(withId(R.id.converter_edittext_from)).perform(clearText(), typeText("7"))
        onView(withId(R.id.converter_spinner_from)).perform(click())
        onView(withText("day")).inRoot(isPlatformPopup()).perform(click())
        onView(withId(R.id.converter_spinner_to)).perform(click())
        onView(withText("week")).inRoot(isPlatformPopup()).perform(click())

        onView(withId(R.id.converter_edittext_to)).check(matches(withText("1E0")))
    }
}
