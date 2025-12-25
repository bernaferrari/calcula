package org.solovyev.android.calculator.keyboard

import android.content.Context
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.EditText

interface FloatingKeyboard {
    fun getRowsCount(landscape: Boolean): Int

    fun getColumnsCount(landscape: Boolean): Int

    fun makeView(landscape: Boolean)

    fun getUser(): User

    interface User {
        fun getContext(): Context

        fun getEditor(): EditText

        fun getKeyboard(): ViewGroup

        fun done()

        fun showIme()

        fun isVibrateOnKeypress(): Boolean

        fun getTypeface(): Typeface
    }
}
