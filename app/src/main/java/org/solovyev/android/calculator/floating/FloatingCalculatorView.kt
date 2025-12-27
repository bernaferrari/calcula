package org.solovyev.android.calculator.floating

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.Display
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.Keyboard
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.di.AppEntryPoint
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.ui.compose.components.KeyboardActions
import org.solovyev.android.calculator.ui.compose.components.KeyboardMode
import org.solovyev.android.calculator.ui.compose.floating.FloatingCalculatorOverlay
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme
import kotlin.math.abs

class FloatingCalculatorView(
    context: Context,
    initialState: State,
    private val listener: FloatingViewListener
) {
    private val context: Context
    private val root: ComposeView
    private val state: State
    private var attached = false
    private var initialized = false
    private var minimized = false
    private var shown = false

    private val keyboard: Keyboard
    private val editor: Editor
    private val display: Display
    private val appPreferences: AppPreferences
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val displayStateFlow = MutableStateFlow(DisplayState.empty())
    private val editorStateFlow = MutableStateFlow(EditorState.empty())
    private val foldedState = MutableStateFlow(false)
    private var headerHeightPx = 0

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppEntryPoint::class.java
        )
        keyboard = entryPoint.keyboard()
        editor = entryPoint.editor()
        display = entryPoint.display()
        appPreferences = entryPoint.appPreferences()
        this.context = context
        root = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        }

        val persistedState = appPreferences.floating.getWidthBlocking()?.let { width ->
            val height = appPreferences.floating.getHeightBlocking() ?: return@let null
            val x = appPreferences.floating.getXBlocking() ?: 0
            val y = appPreferences.floating.getYBlocking() ?: 0
            State(width, height, x, y)
        }
        this.state = persistedState ?: initialState

        val dm = context.resources.displayMetrics
        headerHeightPx = App.toPixels(dm, 56f)
    }

    fun updateDisplayState(displayState: DisplayState) {
        displayStateFlow.value = displayState
    }

    fun updateEditorState(editorState: EditorState) {
        editorStateFlow.value = editorState
    }

    private fun setHeight(height: Int) {
        val params = root.layoutParams as WindowManager.LayoutParams
        params.height = height
        windowManager.updateViewLayout(root, params)
    }

    private fun init() {
        if (initialized) return

        root.setContent {
            val displayState by displayStateFlow.collectAsState()
            val editorState by editorStateFlow.collectAsState()
            val themePreference by appPreferences.settings.theme.collectAsState(
                initial = appPreferences.settings.getThemeBlocking()
            )
            val modePreference by appPreferences.settings.mode.collectAsState(
                initial = appPreferences.settings.getModeBlocking()
            )
            val folded by foldedState.collectAsState()
            val highlightExpressions by appPreferences.settings.highlightExpressions.collectAsState(
                initial = appPreferences.settings.getHighlightExpressionsBlocking()
            )
            val highContrast by appPreferences.settings.highContrast.collectAsState(
                initial = appPreferences.settings.getHighContrastBlocking()
            )
            val vibrateOnKeypress by appPreferences.settings.vibrateOnKeypress.collectAsState(
                initial = appPreferences.settings.vibrateOnKeypressBlocking()
            )
            val keyboardMode = if (modePreference == Preferences.Gui.Mode.engineer) {
                KeyboardMode.ENGINEER
            } else {
                KeyboardMode.SIMPLE
            }
            val actions = remember { FloatingKeyboardActions(keyboard) }

            CalculatorTheme(theme = themePreference) {
                FloatingCalculatorOverlay(
                    displayState = displayState,
                    editorState = editorState,
                    keyboardMode = keyboardMode,
                    highlightExpressions = highlightExpressions,
                    highContrast = highContrast,
                    hapticsEnabled = vibrateOnKeypress,
                    keyboardActions = actions,
                    onEditorTextChange = { text, selection -> editor.setText(text, selection) },
                    onEditorSelectionChange = { selection -> editor.setSelection(selection) },
                    onToggleFold = { toggleFold() },
                    onMinimize = { minimize() },
                    onClose = { hide() },
                    isFolded = folded,
                    onDrag = { dx, dy -> updatePositionBy(dx, dy) },
                    onHeaderHeightChanged = { height -> updateHeaderHeight(height) },
                    title = context.getString(R.string.cpp_app_name)
                )
            }
        }

        initialized = true
    }

    fun show() {
        if (shown) return
        init()
        attach()
        shown = true
    }

    private fun attach() {
        if (!attached) {
            val params = makeLayoutParams().apply {
                width = state.width
                height = state.height
                x = state.x
                y = state.y
                gravity = Gravity.TOP or Gravity.LEFT
            }
            windowManager.addView(root, params)
            attached = true
        }
    }

    private fun detach() {
        if (attached) {
            windowManager.removeView(root)
            attached = false
        }
    }

    private fun toggleFold() {
        if (foldedState.value) {
            unfold()
        } else {
            fold()
        }
    }

    private fun fold() {
        if (!foldedState.value) {
            foldedState.value = true
            setHeight(headerHeightPx)
        }
    }

    private fun unfold() {
        if (foldedState.value) {
            foldedState.value = false
            setHeight(state.height)
        }
    }

    private fun updateHeaderHeight(height: Int) {
        if (height <= 0) return
        headerHeightPx = height
        if (foldedState.value) {
            setHeight(headerHeightPx)
        }
    }

    private fun minimize() {
        if (!minimized) {
            saveState()
            detach()
            listener.onViewMinimized()
            minimized = true
        }
    }

    fun hide() {
        if (!shown) return
        saveState()
        detach()
        listener.onViewHidden()
        shown = false
    }

    private fun saveState() {
        val current = getState()
        scope.launch {
            appPreferences.floating.setSize(current.width, current.height)
            appPreferences.floating.setPosition(current.x, current.y)
        }
    }

    private fun updatePositionBy(dx: Float, dy: Float) {
        if (abs(dx) < 0.5f && abs(dy) < 0.5f) return
        val params = root.layoutParams as WindowManager.LayoutParams
        val dm = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            dm.widthPixels = bounds.width()
            dm.heightPixels = bounds.height()
        } else {
            dm.setTo(context.resources.displayMetrics)
        }
        params.x = (params.x + dx).toInt().coerceIn(0, dm.widthPixels - params.width)
        params.y = (params.y + dy).toInt().coerceIn(0, dm.heightPixels - params.height)
        windowManager.updateViewLayout(root, params)
    }

    private fun getState(): State {
        val params = root.layoutParams as WindowManager.LayoutParams
        return if (!foldedState.value) {
            State(params.width, params.height, params.x, params.y)
        } else {
            State(state.width, state.height, params.x, params.y)
        }
    }

    private val windowManager: WindowManager
        get() = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    data class State(
        var width: Int,
        var height: Int,
        var x: Int,
        var y: Int
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(width)
            parcel.writeInt(height)
            parcel.writeInt(x)
            parcel.writeInt(y)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<State> {
            override fun createFromParcel(parcel: Parcel): State = State(parcel)
            override fun newArray(size: Int): Array<State?> = arrayOfNulls(size)
        }
    }

    private class FloatingKeyboardActions(
        private val keyboard: Keyboard
    ) : KeyboardActions {
        override fun onNumberClick(number: String) {
            keyboard.buttonPressed(number)
        }

        override fun onOperatorClick(operator: String) {
            keyboard.buttonPressed(operator)
        }

        override fun onFunctionClick(function: String) {
            keyboard.buttonPressed(function)
        }

        override fun onSpecialClick(action: String) {
            keyboard.buttonPressed(action)
        }

        override fun onClear() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.clear.action)
        }

        override fun onDelete() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.erase.action)
        }

        override fun onEquals() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.equals.action)
        }

        override fun onMemoryRecall() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.memory.action)
        }

        override fun onMemoryPlus() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.memory_plus.action)
        }

        override fun onMemoryMinus() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.memory_minus.action)
        }

        override fun onMemoryClear() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.memory_clear.action)
        }

        override fun onCursorLeft() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.cursor_left.action)
        }

        override fun onCursorRight() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.cursor_right.action)
        }

        override fun onCursorToStart() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.cursor_to_start.action)
        }

        override fun onCursorToEnd() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.cursor_to_end.action)
        }

        override fun onCopy() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.copy.action)
        }

        override fun onPaste() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.paste.action)
        }

        override fun onOpenVars() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.vars.action)
        }

        override fun onOpenFunctions() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.functions.action)
        }

        override fun onOpenHistory() {
            keyboard.buttonPressed(org.solovyev.android.calculator.buttons.CppSpecialButton.history.action)
        }
    }

    companion object {
        fun isOverlayPermissionGranted(context: Context): Boolean {
            return try {
                val application = context.applicationContext
                val wm = application.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                    ?: return false
                val view = View(application)
                wm.addView(view, makeLayoutParams())
                wm.removeView(view)
                true
            } catch (e: Exception) {
                false
            }
        }

        private fun makeLayoutParams(): WindowManager.LayoutParams {
            return WindowManager.LayoutParams(
                TYPE_APPLICATION_OVERLAY,
                FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCH_MODAL or FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )
        }
    }
}
