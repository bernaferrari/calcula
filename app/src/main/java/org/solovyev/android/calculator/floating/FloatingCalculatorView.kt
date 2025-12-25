/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.floating

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
import android.view.HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
import android.view.HapticFeedbackConstants.KEYBOARD_TAP
import android.view.HapticFeedbackConstants.LONG_PRESS
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
import android.widget.ImageView
import dagger.hilt.android.EntryPointAccessors
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.DisplayView
import org.solovyev.android.calculator.Editor
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.EditorView
import org.solovyev.android.calculator.Keyboard
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.buttons.CppButton
import org.solovyev.android.calculator.di.AppEntryPoint
import org.solovyev.android.calculator.keyboard.BaseKeyboardUi
import org.solovyev.android.views.Adjuster
import kotlin.math.abs

class FloatingCalculatorView(
    context: Context,
    initialState: State,
    private val listener: FloatingViewListener
) {
    private val context: Context
    private val root: View
    private val state: State
    private var content: View? = null
    private var header: View? = null
    private var headerTitle: ImageView? = null
    private var headerTitleDrawable: Drawable? = null
    private var editorView: EditorView? = null
    private var displayView: DisplayView? = null
    private var minimized = false
    private var attached = false
    private var folded = false
    private var initialized = false
    private var shown = false

    private val keyboard: Keyboard
    private val editor: Editor
    private val preferences: SharedPreferences
    private val typeface: Typeface
    private val myPreferences: SharedPreferences

    init {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppEntryPoint::class.java
        )
        keyboard = entryPoint.keyboard()
        editor = entryPoint.editor()
        preferences = entryPoint.preferences()
        typeface = entryPoint.typeface()
        myPreferences = entryPoint.floatingPreferences()

        val theme = Preferences.Onscreen.theme.getPreferenceNoError(preferences) ?: Preferences.SimpleTheme.default_theme
        val appTheme = Preferences.Gui.theme.getPreferenceNoError(preferences) ?: Preferences.Gui.Theme.material_theme
        val resolvedTheme = theme.resolveThemeFor(appTheme)
        this.context = ContextThemeWrapper(
            context,
            if (resolvedTheme.light) R.style.Cpp_Theme_Light else R.style.Cpp_Theme
        )
        this.root = View.inflate(this.context, resolvedTheme.getOnscreenLayout(appTheme), null)
        BaseActivity.fixFonts(this.root, typeface)

        val persistedState = State.fromPrefs(myPreferences)
        this.state = persistedState ?: initialState
    }

    fun updateDisplayState(displayState: DisplayState) {
        checkInit()
        displayView?.setState(displayState)
    }

    private fun checkInit() {
        check(initialized) { "init() must be called!" }
    }

    fun updateEditorState(editorState: EditorState) {
        checkInit()
        editorView?.setState(editorState)
    }

    private fun setHeight(height: Int) {
        checkInit()

        val params = root.layoutParams as WindowManager.LayoutParams
        params.height = height
        windowManager.updateViewLayout(root, params)
    }

    private fun init() {
        if (initialized) {
            return
        }

        for (widgetButton in CppButton.values()) {
            val button = root.findViewById<View>(widgetButton.id) ?: continue

            button.setOnClickListener {
                if (keyboard.buttonPressed(widgetButton.action)) {
                    if (keyboard.vibrateOnKeypress.value) {
                        it.performHapticFeedback(
                            KEYBOARD_TAP,
                            FLAG_IGNORE_GLOBAL_SETTING or FLAG_IGNORE_VIEW_SETTING
                        )
                    }
                }
                if (widgetButton == CppButton.app) {
                    minimize()
                }
            }

            button.setOnLongClickListener {
                if (keyboard.buttonPressed(widgetButton.actionLong)) {
                    if (keyboard.vibrateOnKeypress.value) {
                        it.performHapticFeedback(
                            LONG_PRESS,
                            FLAG_IGNORE_GLOBAL_SETTING or FLAG_IGNORE_VIEW_SETTING
                        )
                    }
                }
                true
            }

            if (widgetButton == CppButton.erase && button is ImageView) {
                Adjuster.adjustImage(button, BaseKeyboardUi.IMAGE_SCALE_ERASE)
            } else {
                BaseKeyboardUi.adjustButton(button)
            }
        }

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        header = root.findViewById(R.id.onscreen_header)
        headerTitle = header?.findViewById(R.id.onscreen_title)
        headerTitleDrawable = headerTitle?.drawable
        headerTitle?.setImageDrawable(null)
        content = root.findViewById(R.id.onscreen_content)

        displayView = root.findViewById(R.id.calculator_display)

        editorView = root.findViewById<EditorView>(R.id.calculator_editor)?.apply {
            setEditor(editor)
        }

        root.findViewById<View>(R.id.onscreen_fold_button)?.setOnClickListener {
            if (folded) {
                unfold()
            } else {
                fold()
            }
        }

        root.findViewById<View>(R.id.onscreen_minimize_button)?.setOnClickListener {
            minimize()
        }

        root.findViewById<View>(R.id.onscreen_close_button)?.setOnClickListener {
            hide()
        }

        headerTitle?.setOnTouchListener(MyTouchListener(wm, root))

        initialized = true
    }

    fun show() {
        if (shown) {
            return
        }
        init()
        attach()
        shown = true
    }

    private fun attach() {
        checkInit()

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (!attached) {
            val params = makeLayoutParams().apply {
                width = state.width
                height = state.height
                x = state.x
                y = state.y
                gravity = Gravity.TOP or Gravity.LEFT
            }
            wm.addView(root, params)
            attached = true
        }
    }

    private fun fold() {
        if (!folded) {
            headerTitle?.setImageDrawable(headerTitleDrawable)
            val r = header?.resources
            val newHeight = (header?.height ?: 0) + 2 * (r?.getDimensionPixelSize(
                R.dimen.cpp_onscreen_main_padding
            ) ?: 0)
            content?.visibility = View.GONE
            setHeight(newHeight)
            folded = true
        }
    }

    private fun unfold() {
        if (folded) {
            headerTitle?.setImageDrawable(null)
            content?.visibility = View.VISIBLE
            setHeight(state.height)
            folded = false
        }
    }

    private fun detach() {
        checkInit()

        if (attached) {
            windowManager.removeView(root)
            attached = false
        }
    }

    private fun minimize() {
        checkInit()
        if (!minimized) {
            saveState()
            detach()
            listener.onViewMinimized()
            minimized = true
        }
    }

    fun hide() {
        checkInit()
        if (!shown) {
            return
        }
        saveState()
        detach()
        listener.onViewHidden()
        shown = false
    }

    private fun saveState() {
        val editor = myPreferences.edit()
        getState().save(editor)
        editor.apply()
    }

    private val windowManager: WindowManager
        get() = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private fun getState(): State {
        val params = root.layoutParams as WindowManager.LayoutParams
        return if (!folded) {
            State(params.width, params.height, params.x, params.y)
        } else {
            State(state.width, state.height, params.x, params.y)
        }
    }

    private class MyTouchListener(
        private val wm: WindowManager,
        private val view: View
    ) : View.OnTouchListener {
        private var orientation: Int = 0
        private var x0 = 0f
        private var y0 = 0f
        private var lastMoveTime = 0L
        private val dm = DisplayMetrics()

        init {
            onDisplayChanged()
        }

        private fun onDisplayChanged() {
            val dd = wm.defaultDisplay
            @Suppress("DEPRECATION")
            orientation = dd.orientation
            dd.getMetrics(dm)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            @Suppress("DEPRECATION")
            if (orientation != wm.defaultDisplay.orientation) {
                // orientation has changed => we need to check display width/height each time window moved
                onDisplayChanged()
            }

            val x1 = event.rawX
            val y1 = event.rawY

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x0 = x1
                    y0 = y1
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val now = System.currentTimeMillis()
                    if (now - lastMoveTime >= TIME_EPS) {
                        lastMoveTime = now
                        processMove(x1, y1)
                    }
                    return true
                }
            }

            return false
        }

        private fun processMove(x1: Float, y1: Float) {
            val Δx = x1 - x0
            val Δy = y1 - y0

            val params = view.layoutParams as WindowManager.LayoutParams

            val xInBounds = isDistanceInBounds(Δx)
            val yInBounds = isDistanceInBounds(Δy)
            if (xInBounds || yInBounds) {
                if (xInBounds) {
                    params.x = (params.x + Δx).toInt()
                }

                if (yInBounds) {
                    params.y = (params.y + Δy).toInt()
                }

                params.x = params.x.coerceIn(0, dm.widthPixels - params.width)
                params.y = params.y.coerceIn(0, dm.heightPixels - params.height)

                wm.updateViewLayout(view, params)

                if (xInBounds) {
                    x0 = x1
                }

                if (yInBounds) {
                    y0 = y1
                }
            }
        }

        private fun isDistanceInBounds(δ: Float): Boolean {
            val distance = abs(δ)
            return distance >= DIST_EPS && distance < DIST_MAX
        }

        companion object {
            private const val DIST_EPS = 0f
            private const val DIST_MAX = 100000f
            private const val TIME_EPS = 0L
        }
    }

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

        private constructor(prefs: SharedPreferences) : this(
            prefs.getInt("width", 200),
            prefs.getInt("height", 400),
            prefs.getInt("x", 0),
            prefs.getInt("y", 0)
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(width)
            parcel.writeInt(height)
            parcel.writeInt(x)
            parcel.writeInt(y)
        }

        override fun describeContents(): Int = 0

        fun save(editor: SharedPreferences.Editor) {
            editor.putInt("width", width)
            editor.putInt("height", height)
            editor.putInt("x", x)
            editor.putInt("y", y)
        }

        companion object CREATOR : Parcelable.Creator<State> {
            override fun createFromParcel(parcel: Parcel): State = State(parcel)
            override fun newArray(size: Int): Array<State?> = arrayOfNulls(size)

            fun fromPrefs(prefs: SharedPreferences): State? {
                return if (!prefs.contains("width")) {
                    null
                } else {
                    State(prefs)
                }
            }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    TYPE_SYSTEM_ALERT
                },
                FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCH_MODAL or FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )
        }
    }
}
