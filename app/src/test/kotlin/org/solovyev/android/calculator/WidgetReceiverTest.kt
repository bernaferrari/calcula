package org.solovyev.android.calculator

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment.application
import org.solovyev.android.calculator.buttons.CppButton.four
import org.solovyev.android.calculator.history.History

@RunWith(RobolectricTestRunner::class)
class WidgetReceiverTest {

    private lateinit var keyboard: Keyboard
    private lateinit var history: History
    private lateinit var widgetReceiver: WidgetReceiver

    @Before
    fun setUp() {
        widgetReceiver = WidgetReceiver()
        widgetReceiver.keyboard = mock(Keyboard::class.java).also { keyboard = it }
        widgetReceiver.history = mock(History::class.java).also { history = it }
        `when`(history.loaded).thenReturn(MutableStateFlow(true))
        `when`(keyboard.vibrateOnKeypress).thenReturn(MutableStateFlow(false))
    }

    @Test
    fun testShouldPressButtonOnIntent() {
        val intent = WidgetReceiver.newButtonClickedIntent(application, four)
        widgetReceiver.handleIntent(application, intent)

        verify(keyboard).buttonPressed(Mockito.anyString())
        verify(keyboard).buttonPressed("4")
    }

    @Test
    fun testShouldDoNothingIfButtonInvalid() {
        val intent = Intent(application, WidgetReceiver::class.java)
        intent.action = WidgetReceiver.ACTION_BUTTON_PRESSED
        intent.putExtra(WidgetReceiver.ACTION_BUTTON_ID_EXTRA, -1)
        widgetReceiver.handleIntent(application, intent)

        verify(keyboard, never()).buttonPressed(Mockito.anyString())
    }
}
