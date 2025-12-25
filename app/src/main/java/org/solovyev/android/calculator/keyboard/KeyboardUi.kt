package org.solovyev.android.calculator.keyboard

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.view.View
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import jscl.NumeralBase
import org.solovyev.android.calculator.Display
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.Keyboard
import org.solovyev.android.calculator.R
import org.solovyev.android.views.dragbutton.DirectionDragButton
import org.solovyev.android.views.dragbutton.DragDirection
import javax.inject.Inject

class KeyboardUi @Inject constructor(
    application: Application
) : BaseKeyboardUi(application) {

    @Inject
    lateinit var engine: Engine

    @Inject
    lateinit var display: Display

    @Inject
    lateinit var bus: Bus

    @Inject
    lateinit var partialUi: PartialKeyboardUi

    lateinit var button0: DirectionDragButton
    lateinit var button1: DirectionDragButton
    lateinit var button2: DirectionDragButton
    lateinit var button3: DirectionDragButton
    lateinit var button4: DirectionDragButton
    lateinit var button5: DirectionDragButton
    lateinit var button6: DirectionDragButton
    lateinit var button7: DirectionDragButton
    lateinit var button8: DirectionDragButton
    lateinit var button9: DirectionDragButton

    lateinit var variablesButton: DirectionDragButton
    var operatorsButton: DirectionDragButton? = null
    lateinit var functionsButton: DirectionDragButton
    lateinit var historyButton: DirectionDragButton
    lateinit var multiplicationButton: DirectionDragButton
    lateinit var plusButton: DirectionDragButton
    lateinit var subtractionButton: DirectionDragButton
    lateinit var divisionButton: DirectionDragButton
    lateinit var periodButton: DirectionDragButton
    lateinit var bracketsButton: DirectionDragButton
    var likeButton: DirectionDragButton? = null
    var percentButton: DirectionDragButton? = null
    var memoryButton: DirectionDragButton? = null

    fun updateNumberMode(mode: NumeralBase) {
        val hex = mode == NumeralBase.hex
        button1.setShowDirectionText(DragDirection.left, hex)
        button2.setShowDirectionText(DragDirection.left, hex)
        button3.setShowDirectionText(DragDirection.left, hex)
        button4.setShowDirectionText(DragDirection.left, hex)
        button5.setShowDirectionText(DragDirection.left, hex)
        button6.setShowDirectionText(DragDirection.left, hex)
    }

    override fun onCreateView(activity: Activity, view: View) {
        super.onCreateView(activity, view)
        partialUi.onCreateView(activity, view)

        button0 = view.findViewById(R.id.cpp_button_0)
        button1 = view.findViewById(R.id.cpp_button_1)
        button2 = view.findViewById(R.id.cpp_button_2)
        button3 = view.findViewById(R.id.cpp_button_3)
        button4 = view.findViewById(R.id.cpp_button_4)
        button5 = view.findViewById(R.id.cpp_button_5)
        button6 = view.findViewById(R.id.cpp_button_6)
        button7 = view.findViewById(R.id.cpp_button_7)
        button8 = view.findViewById(R.id.cpp_button_8)
        button9 = view.findViewById(R.id.cpp_button_9)

        variablesButton = view.findViewById(R.id.cpp_button_vars)
        operatorsButton = view.findViewById(R.id.cpp_button_operators)
        functionsButton = view.findViewById(R.id.cpp_button_functions)
        historyButton = view.findViewById(R.id.cpp_button_history)
        multiplicationButton = view.findViewById(R.id.cpp_button_multiplication)
        plusButton = view.findViewById(R.id.cpp_button_plus)
        subtractionButton = view.findViewById(R.id.cpp_button_subtraction)
        divisionButton = view.findViewById(R.id.cpp_button_division)
        periodButton = view.findViewById(R.id.cpp_button_period)
        bracketsButton = view.findViewById(R.id.cpp_button_round_brackets)
        likeButton = view.findViewById(R.id.cpp_button_like)
        percentButton = view.findViewById(R.id.cpp_button_percent)
        memoryButton = view.findViewById(R.id.cpp_button_memory)

        prepareButton(variablesButton)
        prepareButton(operatorsButton)
        prepareButton(functionsButton)
        prepareButton(historyButton)

        prepareButton(multiplicationButton)
        prepareButton(plusButton)
        prepareButton(subtractionButton)
        prepareButton(divisionButton)

        prepareButton(periodButton)
        prepareButton(bracketsButton)
        prepareButton(percentButton)

        prepareButton(button0)
        prepareButton(button1)
        prepareButton(button2)
        prepareButton(button3)
        prepareButton(button4)
        prepareButton(button5)
        prepareButton(button6)
        prepareButton(button7)
        prepareButton(button8)
        prepareButton(button9)

        prepareButton(likeButton)
        prepareButton(memoryButton)

        if (isSimpleMode()) {
            hideText(button1, DragDirection.down)
            hideText(button2, DragDirection.down)
            hideText(button3, DragDirection.down)
            hideText(button4, DragDirection.down)
            hideText(button5, DragDirection.down)
            hideText(button6, DragDirection.up)
            hideText(button7, DragDirection.left, DragDirection.up, DragDirection.down)
            hideText(button8, DragDirection.left, DragDirection.up, DragDirection.down)
            hideText(button9, DragDirection.left)
            hideText(multiplicationButton, DragDirection.left)
            hideText(plusButton, DragDirection.up)
            hideText(functionsButton, DragDirection.up, DragDirection.down)
        }

        multiplicationButton.text = engine.multiplicationSign.value
        updateNumberMode(keyboard.numberMode.value)
        bus.register(this)
    }

    override fun onDestroyView() {
        bus.unregister(this)
        partialUi.onDestroyView()
        super.onDestroyView()
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String?) {
        super.onSharedPreferenceChanged(preferences, key)
        key ?: return
        if (Engine.Preferences.multiplicationSign.isSameKey(key)) {
            multiplicationButton.text = Engine.Preferences.multiplicationSign.getPreference(preferences) ?: "×"
        }
    }

    @Subscribe
    fun onNumberModeChanged(e: Keyboard.NumberModeChangedEvent) {
        updateNumberMode(e.mode)
    }
}
