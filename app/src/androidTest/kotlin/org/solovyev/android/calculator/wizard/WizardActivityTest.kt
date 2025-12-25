package org.solovyev.android.calculator.wizard

import android.test.suitebuilder.annotation.LargeTest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import net.slideshare.mobile.test.util.OrientationChangeAction.orientationLandscape
import net.slideshare.mobile.test.util.OrientationChangeAction.orientationPortrait
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.solovyev.android.calculator.R

@RunWith(AndroidJUnit4::class)
@LargeTest
class WizardActivityTest {

    @Rule
    @JvmField
    var rule = ActivityTestRule(WizardActivity::class.java)

    @Test
    fun shouldShowConfirmationDialogOnSkip() {
        onView(withText(R.string.cpp_wizard_skip)).perform(click())
        onView(withText(R.string.cpp_wizard_finish_confirmation_title)).inRoot(isDialog()).check(matches(isDisplayed()))
    }

    @Test
    fun testShouldGoThroughTheWizard() {
        onView(withText(R.string.cpp_wizard_start)).perform(click())
        onView(withId(R.id.pager)).check(matches(allOf(
                hasDescendant(withText(R.string.cpp_wizard_mode_title)),
                not(hasDescendant(withText(R.string.cpp_wizard_welcome_title))))))

        onView(withId(R.id.pager)).perform(swipeLeft())
        onView(withId(R.id.pager)).check(matches(hasDescendant(withText(R.string.cpp_wizard_theme_title))))

        onView(withId(R.id.pager)).perform(swipeLeft())
        onView(withId(R.id.pager)).check(matches(hasDescendant(withText(R.string.cpp_wizard_onscreen_description))))

        onView(withId(R.id.pager)).perform(swipeLeft())
        onView(withId(R.id.pager)).check(matches(hasDescendant(withText(R.string.cpp_wizard_dragbutton_description))))

        onView(withId(R.id.pager)).perform(swipeLeft())
        onView(withId(R.id.pager)).check(matches(hasDescendant(withText(R.string.cpp_wizard_final_done))))

        onView(withText(R.string.cpp_wizard_final_done)).perform(click())
    }

    @Test
    fun testShouldPreserveStepOnScreenRotation() {
        onView(withId(R.id.pager)).perform(swipeLeft())
        onView(isRoot()).perform(orientationLandscape())
        onView(withId(R.id.pager)).check(matches(hasDescendant(withText(R.string.cpp_wizard_mode_title))))
        onView(isRoot()).perform(orientationPortrait())
        onView(withId(R.id.pager)).check(matches(hasDescendant(withText(R.string.cpp_wizard_mode_title))))
    }
}
