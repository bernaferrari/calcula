package org.solovyev.android.calculator.view

import android.content.Context
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText
import org.solovyev.android.Check

open class EditTextCompat @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextInputEditText(context, attrs, defStyleAttr) {

    fun dontShowSoftInputOnFocusCompat() {
        setShowSoftInputOnFocusCompat(false)
    }

    fun setShowSoftInputOnFocusCompat(show: Boolean) {
        Check.isMainThread()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            showSoftInputOnFocus = show
        } else {
            dontShowSoftInputOnFocusPreLollipop(show)
        }
    }

    private fun dontShowSoftInputOnFocusPreLollipop(show: Boolean) {
        val method = setShowSoftInputOnFocusMethodCompanion
        if (method == null) {
            disableSoftInputFromAppearing()
            return
        }
        try {
            method.invoke(this, show)
        } catch (e: Exception) {
            Log.w(TAG, e.message, e)
        }
    }

    fun disableSoftInputFromAppearing() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setRawInputType(InputType.TYPE_CLASS_TEXT)
            setTextIsSelectable(true)
        } else {
            setRawInputType(InputType.TYPE_NULL)
            isFocusable = true
        }
    }

    companion object {
        private const val TAG = "EditTextCompat"

        private var _setShowSoftInputOnFocusMethod: java.lang.reflect.Method? = null
        private var setShowSoftInputOnFocusMethodChecked = false

        private val setShowSoftInputOnFocusMethodCompanion: java.lang.reflect.Method?
            get() {
                if (setShowSoftInputOnFocusMethodChecked) {
                    return _setShowSoftInputOnFocusMethod
                }
                setShowSoftInputOnFocusMethodChecked = true
                try {
                    _setShowSoftInputOnFocusMethod = EditText::class.java
                        .getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType)
                        .apply { isAccessible = true }
                } catch (e: NoSuchMethodException) {
                    Log.d(TAG, "setShowSoftInputOnFocus was not found...")
                }
                return _setShowSoftInputOnFocusMethod
            }

        @JvmStatic
        fun insert(text: CharSequence, view: EditText) {
            val e = view.text
            val start = maxOf(0, view.selectionStart)
            val end = maxOf(0, view.selectionEnd)
            e.replace(minOf(start, end), maxOf(start, end), text)
        }
    }
}
