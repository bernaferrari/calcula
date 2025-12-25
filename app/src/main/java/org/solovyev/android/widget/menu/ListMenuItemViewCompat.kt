package org.solovyev.android.widget.menu

import android.util.Log
import androidx.appcompat.view.menu.ListMenuItemView
import java.lang.reflect.Field

internal object ListMenuItemViewCompat {

    private var preserveIconSpacingField: Field? = null

    fun setPreserveIconSpacing(view: ListMenuItemView, preserveIconSpacing: Boolean) {
        val field = getPreserveIconSpacingField() ?: return
        try {
            field.set(view, preserveIconSpacing)
        } catch (e: IllegalAccessException) {
            Log.e("CustomListMenuItemView", e.message, e)
        }
    }

    fun getPreserveIconSpacing(view: ListMenuItemView): Boolean {
        val field = getPreserveIconSpacingField() ?: return false
        return try {
            field.getBoolean(view)
        } catch (e: IllegalAccessException) {
            Log.e("CustomListMenuItemView", e.message, e)
            false
        }
    }

    private fun getPreserveIconSpacingField(): Field? {
        preserveIconSpacingField?.let { return it }

        return try {
            ListMenuItemView::class.java.getDeclaredField("mPreserveIconSpacing").apply {
                isAccessible = true
                preserveIconSpacingField = this
            }
        } catch (e: NoSuchFieldException) {
            null
        }
    }
}
