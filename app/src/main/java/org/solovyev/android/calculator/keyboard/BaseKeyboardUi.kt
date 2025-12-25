package org.solovyev.android.calculator.keyboard

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
import android.view.HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
import android.view.HapticFeedbackConstants.KEYBOARD_TAP
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import dagger.hilt.android.EntryPointAccessors
import org.solovyev.android.Check
import org.solovyev.android.calculator.ActivityLauncher
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.Calculator
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.Keyboard
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.buttons.CppButton
import org.solovyev.android.calculator.di.AppEntryPoint
import org.solovyev.android.calculator.memory.Memory
import org.solovyev.android.views.Adjuster
import org.solovyev.android.views.dragbutton.DirectionDragButton
import org.solovyev.android.views.dragbutton.DirectionDragImageButton
import org.solovyev.android.views.dragbutton.DirectionDragListener
import org.solovyev.android.views.dragbutton.DirectionDragView
import org.solovyev.android.views.dragbutton.Drag
import org.solovyev.android.views.dragbutton.DragDirection
import org.solovyev.android.views.dragbutton.DragEvent
import org.solovyev.android.views.dragbutton.DragView

abstract class BaseKeyboardUi(application: Application) :
    SharedPreferences.OnSharedPreferenceChangeListener,
    View.OnClickListener {

    protected val preferences: SharedPreferences
    protected val keyboard: Keyboard
    protected val editor: Editor
    protected val calculator: Calculator
    protected val launcher: ActivityLauncher
    protected val memory: Memory

    protected var orientation = ORIENTATION_PORTRAIT

    private val dragButtons = mutableListOf<DragView>()
    protected val listener: DirectionDragListener
    private var textSize: Int = 0
    private var mode: Preferences.Gui.Mode = Preferences.Gui.Mode.simple
    private val textScale: Float

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            application,
            AppEntryPoint::class.java
        )
        preferences = entryPoint.preferences()
        keyboard = entryPoint.keyboard()
        editor = entryPoint.editor()
        calculator = entryPoint.calculator()
        launcher = entryPoint.launcher()
        memory = entryPoint.memory()

        listener = object : DirectionDragListener(application) {
            override fun onDrag(view: View, event: DragEvent, direction: DragDirection): Boolean {
                if (!Drag.hasDirectionText(view, direction)) {
                    return false
                }
                val dragView = view as DirectionDragView
                val text = dragView.getText(direction).value
                if (TextUtils.isEmpty(text)) {
                    // hasDirectionText should return false for empty text
                    Check.shouldNotHappen()
                    return false
                }
                keyboard.buttonPressed(text)
                return true
            }
        }
        textScale = getTextScale(application)
    }

    override fun onClick(v: View) {
        val button = CppButton.getById(v.id)
        if (button == null) {
            Check.shouldNotHappen()
            return
        }
        onClick(v, button.action)
    }

    open fun onCreateView(activity: Activity, view: View) {
        preferences.registerOnSharedPreferenceChangeListener(this)

        orientation = App.getScreenOrientation(activity)
        mode = Preferences.Gui.mode.getPreferenceNoError(preferences) ?: Preferences.Gui.Mode.simple
        textSize = calculateTextSize(activity)
    }

    protected fun prepareButton(button: View?) {
        button ?: return
        // we call android.view.View.performHapticFeedback(int, int) from #onClick
        button.isHapticFeedbackEnabled = false
        button.setOnClickListener(this)
    }

    protected fun prepareButton(button: DirectionDragButton?) {
        button ?: return
        dragButtons.add(button)
        button.setVibrateOnDrag(keyboard.vibrateOnKeypress.value)
        button.setHighContrast(keyboard.highContrast.value)
        prepareButton(button as View)
        button.setOnDragListener(listener)
        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        Adjuster.adjustText(button, textScale)
    }

    protected fun hideText(button: DirectionDragView?, vararg directions: DragDirection) {
        button ?: return
        directions.forEach { direction ->
            hideText(button, direction)
        }
    }

    protected fun hideText(button: DirectionDragView?, direction: DragDirection) {
        button ?: return
        button.getText(direction).isVisible = false
    }

    open fun onDestroyView() {
        dragButtons.clear()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String?) {
        key ?: return
        when {
            Preferences.Gui.vibrateOnKeypress.isSameKey(key) -> {
                val vibrate = Preferences.Gui.vibrateOnKeypress.getPreference(preferences) ?: true
                dragButtons.forEach { it.setVibrateOnDrag(vibrate) }
            }
            Preferences.Gui.highContrast.isSameKey(key) -> {
                val highContrast = Preferences.Gui.highContrast.getPreference(preferences) ?: false
                dragButtons.forEach { it.setHighContrast(highContrast) }
            }
        }
    }

    protected fun isSimpleMode(): Boolean = mode == Preferences.Gui.Mode.simple

    protected fun onClick(v: View, s: String) {
        if (!keyboard.buttonPressed(s)) {
            return
        }
        if (!keyboard.vibrateOnKeypress.value) {
            return
        }
        v.performHapticFeedback(KEYBOARD_TAP, FLAG_IGNORE_GLOBAL_SETTING or FLAG_IGNORE_VIEW_SETTING)
    }

    private class AdjusterHelper : Adjuster.Helper<DirectionDragImageButton> {
        override fun apply(view: DirectionDragImageButton, textSize: Float) {
            view.setTextSize(textSize)
        }

        override fun getTextSize(view: DirectionDragImageButton): Float = view.getTextSize()

        companion object {
            val instance = AdjusterHelper()
        }
    }

    companion object {
        const val IMAGE_SCALE = 0.5f
        const val IMAGE_SCALE_ERASE = 0.4f

        fun getTextScale(context: Context): Float =
            if (App.isTablet(context)) 0.4f else 0.5f

        fun adjustButton(button: View) {
            when (button) {
                is TextView -> Adjuster.adjustText(button, getTextScale(button.context))
                is DirectionDragImageButton -> {
                    Adjuster.adjustText(
                        button,
                        AdjusterHelper.instance,
                        getTextScale(button.context),
                        0f
                    )
                    Adjuster.adjustImage(button as ImageView, IMAGE_SCALE)
                }
                is ImageView -> Adjuster.adjustImage(button, IMAGE_SCALE)
            }
        }

        fun calculateTextSize(activity: Activity): Int {
            val portrait = App.getScreenOrientation(activity) == ORIENTATION_PORTRAIT
            val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metrics)
            val buttonsCount = if (portrait) 5 else 4
            val buttonsWeight = if (portrait) (2 + 1 + buttonsCount) else (2 + buttonsCount)
            val buttonSize = metrics.heightPixels / buttonsWeight
            return 5 * buttonSize / 12
        }
    }
}
