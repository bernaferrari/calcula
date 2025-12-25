package org.solovyev.android.calculator.keyboard

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.view.View
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.view.EditorLongClickEraser
import org.solovyev.android.views.dragbutton.DirectionDragButton
import org.solovyev.android.views.dragbutton.DragDirection
import javax.inject.Inject

class PartialKeyboardUi @Inject constructor(
    application: Application
) : BaseKeyboardUi(application) {

    var rightButton: DirectionDragButton? = null
    var leftButton: DirectionDragButton? = null
    var clearButton: DirectionDragButton? = null
    var eraseButton: DirectionDragButton? = null
    var equalsButton: DirectionDragButton? = null
    var longClickEraser: EditorLongClickEraser? = null

    override fun onCreateView(activity: Activity, view: View) {
        super.onCreateView(activity, view)

        rightButton = view.findViewById(R.id.cpp_button_right)
        leftButton = view.findViewById(R.id.cpp_button_left)
        clearButton = view.findViewById(R.id.cpp_button_clear)
        eraseButton = view.findViewById(R.id.cpp_button_erase)
        equalsButton = view.findViewById(R.id.cpp_button_equals)

        prepareButton(rightButton)
        prepareButton(leftButton)
        prepareButton(equalsButton)
        prepareButton(clearButton)

        eraseButton?.let { button ->
            prepareButton(button)
            longClickEraser = EditorLongClickEraser.attachTo(
                button,
                keyboard.vibrateOnKeypress.value,
                editor,
                calculator
            )
        }

        if (isSimpleMode()) {
            hideText(equalsButton, DragDirection.down)
        }
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String?) {
        super.onSharedPreferenceChanged(preferences, key)
        key ?: return
        val eraser = longClickEraser ?: return
        if (Preferences.Gui.vibrateOnKeypress.isSameKey(key)) {
            eraser.vibrateOnKeypress =
                Preferences.Gui.vibrateOnKeypress.getPreference(preferences) ?: true
        }
    }
}
