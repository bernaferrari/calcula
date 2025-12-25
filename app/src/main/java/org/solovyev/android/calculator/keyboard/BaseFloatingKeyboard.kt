package org.solovyev.android.calculator.keyboard

import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.R
import org.solovyev.android.views.dragbutton.DirectionDragButton

abstract class BaseFloatingKeyboard(
    @get:JvmName("userProperty")
    protected val user: FloatingKeyboard.User
) : FloatingKeyboard {

    @ColorInt
    private val textColor: Int

    @ColorInt
    private val textColorSecondary: Int

    private val sidePadding: Int

    @DrawableRes
    private val buttonBackground: Int

    init {
        val resources = user.getContext().resources
        @Suppress("DEPRECATION")
        textColor = resources.getColor(R.color.cpp_button_text)
        @Suppress("DEPRECATION")
        textColorSecondary = resources.getColor(R.color.cpp_button_text)
        sidePadding = resources.getDimensionPixelSize(R.dimen.cpp_button_padding)
        buttonBackground = if (App.getTheme().light) {
            R.drawable.material_button_light
        } else {
            R.drawable.material_button_dark
        }
    }

    override fun getUser(): FloatingKeyboard.User = user

    protected fun makeRow(): LinearLayout {
        val row = LinearLayout(user.getContext()).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0
        ).apply {
            weight = 1f
        }
        user.getKeyboard().addView(row, lp)
        return row
    }

    protected open fun makeButton(@IdRes id: Int, text: String): DirectionDragButton {
        val button = DirectionDragButton(user.getContext())
        fillButton(button, id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            button.isAllCaps = false
        }
        button.text = text
        button.setTextColor(textColor)
        button.setDirectionTextColor(textColorSecondary)
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24f)
        button.setVibrateOnDrag(user.isVibrateOnKeypress())
        if (TextUtils.isEmpty(text)) {
            button.isEnabled = false
        }
        return button
    }

    protected open fun fillButton(button: View, @IdRes id: Int) {
        BaseActivity.setFont(button, user.getTypeface())
        button.id = id
        button.setBackgroundResource(buttonBackground)
        button.setPadding(sidePadding, 1, sidePadding, 1)
        button.isHapticFeedbackEnabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.stateListAnimator = null
        }
    }

    protected fun makeImageButton(@IdRes id: Int, @DrawableRes icon: Int): View {
        val button = ImageButton(user.getContext())
        fillButton(button, id)
        button.setImageResource(icon)
        button.scaleType = ImageView.ScaleType.CENTER_INSIDE
        return button
    }

    protected fun addImageButton(
        row: LinearLayout,
        @IdRes id: Int,
        @DrawableRes icon: Int
    ): View {
        val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT).apply {
            weight = 1f
        }
        val view = makeImageButton(id, icon)
        row.addView(view, lp)
        return view
    }

    protected fun addOperationButton(
        row: LinearLayout,
        @IdRes id: Int,
        text: String
    ): DirectionDragButton {
        val button = addButton(row, id, text)
        button.setBackgroundResource(R.drawable.material_button_light_primary)
        button.setTextColor(Color.WHITE)
        button.setDirectionTextAlpha(0.7f)
        return button
    }

    protected fun addButton(
        row: LinearLayout,
        @IdRes id: Int,
        text: String
    ): DirectionDragButton {
        val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT).apply {
            weight = 1f
        }
        val view = makeButton(id, text)
        row.addView(view, lp)
        return view
    }
}
